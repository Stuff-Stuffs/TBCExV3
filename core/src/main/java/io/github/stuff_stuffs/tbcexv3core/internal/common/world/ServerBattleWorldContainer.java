package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleListenerEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialBoundsBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialParticipantJoinBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.CoreBattleEntityComponents;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

public class ServerBattleWorldContainer implements AutoCloseable {
    private static final long TIMEOUT_TICK_DIFF = 6000;
    private final ServerWorld world;
    private final Map<UUID, Battle> battles;
    private final Object2LongMap<UUID> lastAccessTime;
    private final Map<UUID, DelayedComponents> componentsToApply;
    private final Random random;
    private final RegistryKey<World> worldKey;
    private final ServerBattleWorldDatabase database;
    private final @Nullable Thread serverThread;
    private long tickCount;

    public ServerBattleWorldContainer(final ServerWorld world, final RegistryKey<World> worldKey, final Path directory, @Nullable final Thread thread) {
        this.world = world;
        this.worldKey = worldKey;
        serverThread = thread;
        battles = new Object2ReferenceOpenHashMap<>();
        lastAccessTime = new Object2LongOpenHashMap<>();
        componentsToApply = new Object2ReferenceOpenHashMap<>();
        random = Random.createLocal();
        database = new ServerBattleWorldDatabase(world.getRegistryManager().get(RegistryKeys.BIOME), directory.resolve(worldKey.getValue().toUnderscoreSeparatedString()));
    }

    public Battle getBattle(final UUID uuid) {
        checkThread();
        Battle battle = battles.get(uuid);
        if (battle != null) {
            lastAccessTime.put(uuid, tickCount);
            return battle;
        }
        battle = database.loadBattle(uuid).map(func -> func.create(BattleHandle.of(worldKey, uuid), BattleStateMode.SERVER)).getOrThrow(false, s -> {
            throw new TBCExException(s);
        });
        attachListeners(battle);
        battles.put(uuid, battle);
        lastAccessTime.put(uuid, tickCount);
        return battle;
    }

    private void attachListeners(final Battle battle) {
        final EventMap eventMap = battle.getState().getEventMap();
        eventMap.getEvent(CoreBattleEvents.POST_BATTLE_PARTICIPANT_JOIN_EVENT).registerListener((state, tracer) -> {
            if (state.getEntityComponent(CoreBattleEntityComponents.TRACKED_BATTLE_ENTITY_COMPONENT_TYPE).isPresent()) {
                database.onBattleJoinLeave(state.getUuid(), state.getBattleState().getHandle().getUuid(), true);
            }
        });
        eventMap.getEvent(CoreBattleEvents.POST_BATTLE_PARTICIPANT_LEAVE_EVENT).registerListener((stateView, battleStateView, reason, tracer) -> {
            if (stateView.getEntityComponent(CoreBattleEntityComponents.TRACKED_BATTLE_ENTITY_COMPONENT_TYPE).isPresent()) {
                database.onBattleJoinLeave(stateView.getUuid(), stateView.getHandle().getParent().getUuid(), false);
            }
        });
        eventMap.getEvent(CoreBattleEvents.POST_BATTLE_END_EVENT).registerListener((state, tracer) -> {
            for (final BattleParticipantHandle participant : state.getParticipants()) {
                if (state.getParticipantByHandle(participant).getEntityComponent(CoreBattleEntityComponents.TRACKED_BATTLE_ENTITY_COMPONENT_TYPE).isPresent()) {
                    database.onBattleJoinLeave(participant.getUuid(), state.getHandle().getUuid(), false);
                }
            }
        });
        BattleListenerEvent.EVENT.invoker().attachListeners(battle, world);
    }

    public void tick() {
        checkThread();
        tickCount++;
        final Set<UUID> toRemove = new ObjectOpenHashSet<>();
        for (final Object2LongMap.Entry<UUID> entry : lastAccessTime.object2LongEntrySet()) {
            if (tickCount - entry.getLongValue() > TIMEOUT_TICK_DIFF) {
                toRemove.add(entry.getKey());
            }
        }
        for (final UUID uuid : toRemove) {
            database.saveBattle(uuid, battles.remove(uuid));
            lastAccessTime.removeLong(uuid);
        }
    }

    public BattleHandle createBattle(final Map<BattleEntity, Identifier> entities, final InitialTeamSetupBattleAction teamSetupAction) {
        checkThread();
        final Optional<Box> reduced = entities.keySet().stream().map(BattleEntity::getDefaultBounds).flatMap(bounds -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(bounds.parts(), 0), false)).map(BattleParticipantBounds.Part::box).reduce(Box::union);
        if (reduced.isEmpty()) {
            throw new TBCExException("Tried to create auto-sizing battle with no bounded entities!");
        }
        final Box expanded = reduced.get();
        final BattleBounds bounds = new BattleBounds(expanded);
        return createBattle(entities, teamSetupAction, bounds, 24);
    }

    public BattleHandle createBattle(final Map<BattleEntity, Identifier> entities, final InitialTeamSetupBattleAction teamSetupAction, final BattleBounds bounds, int padding) {
        checkThread();
        for (final BattleEntity entity : entities.keySet()) {
            if (entity instanceof Entity regularEntity && regularEntity.isRemoved()) {
                throw new TBCExException("Tried to add a removed entity to a battle");
            }
        }
        final BattleHandle handle = BattleHandle.of(worldKey, database.findUnusedBattleUuid(random));
        final Battle battle = new BattleImpl(handle, BattleStateMode.SERVER, BattleEnvironmentImpl.Initial.of(bounds, world, padding, Blocks.BARRIER.getDefaultState(), world.getRegistryManager().get(RegistryKeys.BIOME).entryOf(BiomeKeys.PLAINS)));
        battles.put(handle.getUuid(), battle);
        attachListeners(battle);
        battle.pushAction(teamSetupAction);
        battle.pushAction(new InitialBoundsBattleAction(bounds));
        for (final Map.Entry<BattleEntity, Identifier> entry : entities.entrySet()) {
            final BattleEntity entity = entry.getKey();
            final BattleParticipantStateBuilder builder = BattleParticipantStateBuilder.create(entity.getUuid(), entity.getDefaultBounds());
            entity.buildParticipantState(builder);
            final BattleParticipantStateBuilder.Built built = builder.build(entry.getValue());
            final InitialParticipantJoinBattleAction joinBattleAction = new InitialParticipantJoinBattleAction(built);
            battle.pushAction(joinBattleAction);
            if (entry.getKey() instanceof Entity regularEntity) {
                built.onJoin(handle, regularEntity);
            }
        }
        return handle;
    }

    public void pushDelayedPlayerComponent(final UUID playerUuid, final BattleHandle handle, final BattleEntityComponent component) {
        checkThread();
        componentsToApply.computeIfAbsent(playerUuid, database::getDelayedComponents).addDelayedComponent(handle.getUuid(), component);
    }

    public boolean delayedComponent(final UUID uuid, final ServerWorld world) {
        checkThread();
        final DelayedComponents delayedComponents = componentsToApply.computeIfAbsent(uuid, database::getDelayedComponents);
        if (delayedComponents != null) {
            if (delayedComponents.apply(this, world)) {
                componentsToApply.remove(uuid);
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public void close() {
        checkThread();
        for (final Map.Entry<UUID, Battle> entry : battles.entrySet()) {
            final UUID uuid = entry.getKey();
            final Battle battle = entry.getValue();
            database.saveBattle(uuid, battle);
        }
        for (final Map.Entry<UUID, DelayedComponents> entry : componentsToApply.entrySet()) {
            if (entry.getValue().touched) {
                database.saveDelayedComponents(entry.getKey(), entry.getValue().components.size() == 0 ? null : entry.getValue());
            }
        }
    }

    public List<BattleParticipantHandle> getBattles(final UUID entityUuid, final TriState active) {
        checkThread();
        return Arrays.stream(database.getParticipatedBattles(entityUuid, active)).map(id -> BattleParticipantHandle.of(entityUuid, BattleHandle.of(worldKey, id))).toList();
    }

    public static final class DelayedComponents {
        private static final int MAX_ATTEMPTS = 32;
        final Map<UUID, List<BattleEntityComponent>> components;
        private final Object2IntMap<UUID> attempts;
        private boolean touched = false;

        public DelayedComponents(final Map<UUID, List<BattleEntityComponent>> components) {
            this.components = new Object2ReferenceOpenHashMap<>(components);
            attempts = new Object2IntOpenHashMap<>();
        }

        public void addDelayedComponent(final UUID battleUuid, final BattleEntityComponent component) {
            components.computeIfAbsent(battleUuid, i -> new ArrayList<>()).add(component);
            touched = true;
        }

        public boolean apply(final ServerBattleWorldContainer container, final ServerWorld world) {
            final Set<UUID> toRemove = new ObjectOpenHashSet<>();
            for (final Map.Entry<UUID, List<BattleEntityComponent>> entry : components.entrySet()) {
                final UUID key = entry.getKey();
                final Battle battle = container.getBattle(key);
                if (battle == null) {
                    if (attempts.put(key, attempts.getOrDefault(key, 0) + 1) >= MAX_ATTEMPTS) {
                        toRemove.add(key);
                        touched = true;
                    }
                } else {
                    for (final BattleEntityComponent component : entry.getValue()) {
                        component.onLeave(battle, world);
                    }
                    toRemove.add(key);
                    touched = true;
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

    private void checkThread() {
        if (serverThread != null && Thread.currentThread() != serverThread) {
            throw new TBCExException("Battle access only allowed on server thread!");
        }
    }
}
