package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialParticipantJoinBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

//TODO URGENT persistent delayed components and entity battle storage
public class ServerBattleWorldContainer {
    private static final long TIMEOUT_TICK_DIFF = 6000;
    private final Map<UUID, Battle> battles;
    private final Object2LongMap<UUID> lastAccessTime;
    private final Map<UUID, DelayedComponents> componentsToApply;
    private final Random random;
    private final RegistryKey<World> worldKey;
    private final Path directory;
    private long tickCount;

    public ServerBattleWorldContainer(final RegistryKey<World> worldKey, final Path directory) {
        this.worldKey = worldKey;
        battles = new Object2ReferenceOpenHashMap<>();
        lastAccessTime = new Object2LongOpenHashMap<>();
        componentsToApply = new Object2ReferenceOpenHashMap<>();
        this.directory = directory;
        random = Random.createLocal();
    }

    public Battle getBattle(final UUID uuid) {
        Battle battle = battles.get(uuid);
        if (battle != null) {
            lastAccessTime.put(uuid, tickCount);
            return battle;
        }
        battle = loadBattle(uuid);
        if (battle == null) {
            return null;
        } else {
            battles.put(uuid, battle);
            lastAccessTime.put(uuid, tickCount);
            return battle;
        }
    }

    public void tick() {
        tickCount++;
        final Set<UUID> toRemove = new ObjectOpenHashSet<>();
        for (final Object2LongMap.Entry<UUID> entry : lastAccessTime.object2LongEntrySet()) {
            if (tickCount - entry.getLongValue() > TIMEOUT_TICK_DIFF) {
                toRemove.add(entry.getKey());
            }
        }
        for (final UUID uuid : toRemove) {
            if (saveBattle(uuid, battles.get(uuid))) {
                battles.remove(uuid);
                lastAccessTime.removeLong(uuid);
            }
        }
    }

    public BattleHandle createBattle(final Map<BattleEntity, Identifier> entities, final InitialTeamSetupBattleAction teamSetupAction) {
        for (final BattleEntity entity : entities.keySet()) {
            if (entity instanceof Entity regularEntity && regularEntity.isRemoved()) {
                throw new TBCExException("Tried to add a removed entity to a battle");
            }
        }
        final BattleHandle handle = BattleHandle.of(worldKey, findUnused());
        final Battle battle = new BattleImpl(handle, BattleStateMode.SERVER);
        battles.put(handle.getUuid(), battle);
        battle.pushAction(teamSetupAction);
        for (final Map.Entry<BattleEntity, Identifier> entry : entities.entrySet()) {
            final BattleEntity entity = entry.getKey();
            final BattleParticipantStateBuilder builder = BattleParticipantStateBuilder.create(entity.getUuid());
            entity.buildParticipantState(builder);
            final BattleParticipantStateBuilder.Built built = builder.build(entry.getValue());
            if (entry instanceof Entity regularEntity) {
                built.onJoin(handle, regularEntity);
            }
            final InitialParticipantJoinBattleAction joinBattleAction = new InitialParticipantJoinBattleAction(built);
            battle.pushAction(joinBattleAction);
        }
        return handle;
    }

    public void pushDelayedComponent(final UUID playerUuid, final BattleHandle handle, final BattleEntityComponent component) {
        componentsToApply.computeIfAbsent(playerUuid, i -> new DelayedComponents()).addDelayedComponent(handle.getUuid(), component);
    }

    public boolean delayedComponent(final UUID uuid, final ServerWorld world) {
        final DelayedComponents delayedComponents = componentsToApply.get(uuid);
        if (delayedComponents != null) {
            if (delayedComponents.apply(this, world)) {
                componentsToApply.remove(uuid);
                return true;
            }
            return false;
        }
        return true;
    }

    private UUID findUnused() {
        UUID uuid;
        do {
            uuid = new UUID(random.nextLong(), random.nextLong());
        } while (!battles.containsKey(uuid) && !Files.exists(directory.resolve(toFileName(uuid))));
        return uuid;
    }

    private boolean saveBattle(final UUID uuid, final Battle battle) {
        final Optional<NbtElement> encoded = Battle.encoder().encode(battle, NbtOps.INSTANCE, NbtOps.INSTANCE.empty()).resultOrPartial(s -> TBCExV3Core.LOGGER.error("Error while saving battle: " + s));
        if (encoded.isPresent()) {
            try (final BufferedWriter writer = Files.newBufferedWriter(directory.resolve(toFileName(uuid)), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
                try (final OutputStream stream = new WriterOutputStream(writer, StandardCharsets.UTF_8)) {
                    NbtIo.writeCompressed((NbtCompound) encoded.get(), stream);
                    return true;
                }
            } catch (final IOException e) {
                TBCExV3Core.LOGGER.error("Error while saving battle!", e);
            }
        }
        return false;
    }

    private @Nullable Battle loadBattle(final UUID uuid) {
        if (!checkExists(uuid)) {
            return null;
        }
        try (final BufferedReader reader = Files.newBufferedReader(directory.resolve(toFileName(uuid)), StandardCharsets.UTF_8)) {
            try (final InputStream stream = new ReaderInputStream(reader, StandardCharsets.UTF_8)) {
                final NbtCompound compound = NbtIo.readCompressed(stream);
                final DataResult<Battle> dataResult = Battle.decoder().decode(NbtOps.INSTANCE, compound).map(Pair::getFirst).map(f -> f.apply(BattleHandle.of(worldKey, uuid), BattleStateMode.SERVER));
                final Optional<Battle> battle = dataResult.resultOrPartial(s -> TBCExV3Core.LOGGER.error("Error while loading battle: " + s));
                return battle.orElse(null);
            }
        } catch (final IOException e) {
            TBCExV3Core.LOGGER.error("Exception while loading battle!", e);
            return null;
        }
    }

    private boolean checkExists(final UUID uuid) {
        final Path path = directory.resolve(toFileName(uuid));
        return Files.exists(path) && Files.isRegularFile(path);
    }

    public void syncPlayer(final ServerPlayerEntity player) {

    }

    private static String toFileName(final UUID uuid) {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getLeastSignificantBits());
        buffer.putLong(uuid.getMostSignificantBits());
        return Base64.getUrlEncoder().encodeToString(buffer.array()) + ".tbcex_battle";
    }

    private static final class DelayedComponents {
        private static final int MAX_ATTEMPTS = 32;
        private final Map<UUID, List<BattleEntityComponent>> components;
        private final Object2IntMap<UUID> attempts;

        private DelayedComponents() {
            components = new Object2ReferenceOpenHashMap<>();
            attempts = new Object2IntOpenHashMap<>();
        }

        public void addDelayedComponent(final UUID battleUuid, final BattleEntityComponent component) {
            components.computeIfAbsent(battleUuid, i -> new ArrayList<>()).add(component);
        }

        public boolean apply(final ServerBattleWorldContainer container, final ServerWorld world) {
            final Set<UUID> toRemove = new ObjectOpenHashSet<>();
            for (final Map.Entry<UUID, List<BattleEntityComponent>> entry : components.entrySet()) {
                final UUID key = entry.getKey();
                final Battle battle = container.getBattle(key);
                if (battle == null) {
                    if (attempts.put(key, attempts.getOrDefault(key, 0) + 1) >= MAX_ATTEMPTS) {
                        toRemove.add(key);
                    }
                } else {
                    for (final BattleEntityComponent component : entry.getValue()) {
                        component.onLeave(battle, world);
                    }
                    toRemove.add(key);
                }
            }
            if (!toRemove.isEmpty()) {
                for (final UUID uuid : toRemove) {
                    components.remove(uuid);
                }
            }
            return components.isEmpty();
        }
    }
}
