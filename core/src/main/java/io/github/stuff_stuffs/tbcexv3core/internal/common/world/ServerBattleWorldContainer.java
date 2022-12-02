package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleListenerEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialParticipantJoinBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.PreBattleParticipantLeaveEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.nio.file.Path;
import java.util.*;

public class ServerBattleWorldContainer implements AutoCloseable {
    private static final long TIMEOUT_TICK_DIFF = 6000;
    private final Map<UUID, Battle> battles;
    private final Object2LongMap<UUID> lastAccessTime;
    private final Map<UUID, DelayedComponents> componentsToApply;
    private final Random random;
    private final RegistryKey<World> worldKey;
    private final ServerBattleWorldDatabase database;
    private long tickCount;
    private boolean closed = false;

    public ServerBattleWorldContainer(final RegistryKey<World> worldKey, final Path directory) {
        this.worldKey = worldKey;
        battles = new Object2ReferenceOpenHashMap<>();
        lastAccessTime = new Object2LongOpenHashMap<>();
        componentsToApply = new Object2ReferenceOpenHashMap<>();
        random = Random.createLocal();
        database = new ServerBattleWorldDatabase(directory.resolve(worldKey.getValue().toUnderscoreSeparatedString()));
    }

    public Battle getBattle(final UUID uuid) {
        Battle battle = battles.get(uuid);
        if (battle != null) {
            lastAccessTime.put(uuid, tickCount);
            return battle;
        }
        battle = database.loadBattle(uuid).map(func -> func.apply(BattleHandle.of(worldKey, uuid), BattleStateMode.SERVER)).getOrThrow(false, s -> {
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
            if (!closed) {
                database.onBattleJoin(state.getUuid(), state.getBattleState().getHandle().getUuid(), true);
            }
        });
        eventMap.getEvent(CoreBattleEvents.POST_BATTLE_PARTICIPANT_LEAVE_EVENT).registerListener((handle, battleStateView, reason, tracer) -> {
            if (!closed) {
                database.onBattleJoin(handle.getUuid(), handle.getParent().getUuid(), false);
            }
        });
        eventMap.getEvent(CoreBattleEvents.POST_BATTLE_END_EVENT).registerListener((state, tracer) -> {
            if (!closed) {
                for (final BattleParticipantHandle participant : state.getParticipants()) {
                    database.onBattleJoin(participant.getUuid(), state.getHandle().getUuid(), false);
                }
            }
        });
        BattleListenerEvent.EVENT.invoker().attachListener(battle);
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
            database.saveBattle(uuid, battles.remove(uuid));
            lastAccessTime.removeLong(uuid);
        }
    }

    public BattleHandle createBattle(final Map<BattleEntity, Identifier> entities, final InitialTeamSetupBattleAction teamSetupAction) {
        for (final BattleEntity entity : entities.keySet()) {
            if (entity instanceof Entity regularEntity && regularEntity.isRemoved()) {
                throw new TBCExException("Tried to add a removed entity to a battle");
            }
        }
        final BattleHandle handle = BattleHandle.of(worldKey, database.findUnusedBattleUuid(random));
        final Battle battle = new BattleImpl(handle, BattleStateMode.SERVER);
        battles.put(handle.getUuid(), battle);
        attachListeners(battle);
        battle.pushAction(teamSetupAction);
        for (final Map.Entry<BattleEntity, Identifier> entry : entities.entrySet()) {
            final BattleEntity entity = entry.getKey();
            final BattleParticipantStateBuilder builder = BattleParticipantStateBuilder.create(entity.getUuid());
            entity.buildParticipantState(builder);
            final BattleParticipantStateBuilder.Built built = builder.build(entry.getValue());
            if (entry.getKey() instanceof Entity regularEntity) {
                built.onJoin(handle, regularEntity);
            }
            final InitialParticipantJoinBattleAction joinBattleAction = new InitialParticipantJoinBattleAction(built);
            battle.pushAction(joinBattleAction);
        }
        return handle;
    }

    public void pushDelayedComponent(final UUID playerUuid, final BattleHandle handle, final BattleEntityComponent component) {
        componentsToApply.computeIfAbsent(playerUuid, database::getDelayedComponents).addDelayedComponent(handle.getUuid(), component);
    }

    public boolean delayedComponent(final UUID uuid, final ServerWorld world) {
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
        closed = true;
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
        database.close();
    }

    public List<BattleParticipantHandle> getBattles(final UUID entityUuid, final TriState active) {
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
}
