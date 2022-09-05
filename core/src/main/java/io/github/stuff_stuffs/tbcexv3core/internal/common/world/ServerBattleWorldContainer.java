package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
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

public class ServerBattleWorldContainer {
    private static final long TIMEOUT_TICK_DIFF = 6000;
    private final Map<UUID, Battle> battles;
    private final Object2LongMap<UUID> lastAccessTime;
    private final Path directory;
    private long tickCount;

    public ServerBattleWorldContainer(final Path directory) {
        battles = new Object2ReferenceOpenHashMap<>();
        lastAccessTime = new Object2LongOpenHashMap<>();
        this.directory = directory;
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
                final DataResult<Battle> dataResult = Battle.decoder().decode(NbtOps.INSTANCE, compound).map(Pair::getFirst).map(f -> f.apply(BattleStateMode.SERVER));
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

    private static String toFileName(final UUID uuid) {
        final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getLeastSignificantBits());
        buffer.putLong(uuid.getMostSignificantBits());
        return Base64.getUrlEncoder().encodeToString(buffer.array()) + ".tbcex_battle";
    }
}
