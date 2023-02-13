package io.github.stuff_stuffs.tbcexv3core.internal.common;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleListenerEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleParticipantLeaveBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.CoreBattleActions;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.CoreBattleEffects;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.*;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment.PostBattleBlockSetEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment.PostBlockStateSetEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment.PreBattleBlockSetEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment.PreBlockStateSetEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.team.PostChangeTeamRelationEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.team.PreChangeTeamRelationEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.CoreBattleParticipantEffects;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.*;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PostEquipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PostUnequipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PreEquipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PreUnequipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health.PostBattleParticipantSetHealthEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health.PreBattleParticipantDeathEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health.PreBattleParticipantSetHealthEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PostGiveBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PostTakeBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PreGiveBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PreTakeBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.CoreBattleParticipantStats;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.*;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.environment.BattleEnvironmentSection;
import io.github.stuff_stuffs.tbcexv3core.internal.common.mixin.AccessorWorldSavePath;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.TBCExV3PlayNetworkingInit;
import io.github.stuff_stuffs.tbcexv3core.internal.common.world.ChunkSectionExtensions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Supplier;

public class TBCExV3Core implements ModInitializer {
    public static final String MOD_ID = "tbcexv3core";
    public static final WorldSavePath TBCEX_WORLD_SAVE_PATH = AccessorWorldSavePath.create("tbcex");
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static Supplier<@Nullable Logger> CLIENT_LOGGER = null;

    public static void applyInitialToWorld(final ServerWorld world, final BattleEnvironmentImpl.Initial initial, final BlockPos start) {
        final int sectionMinX = initial.min().getX() >> 4;
        final int sectionMinY = initial.min().getY() >> 4;
        final int sectionMinZ = initial.min().getZ() >> 4;
        final int sectionMaxX = (initial.max().getX() + 15) >> 4;
        final int sectionMaxY = (initial.max().getY() + 15) >> 4;
        final int sectionMaxZ = (initial.max().getZ() + 15) >> 4;
        final int startX = start.getX() >> 4;
        final int startZ = start.getZ() >> 4;
        final Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        final ServerLightingProvider provider = world.getChunkManager().getLightingProvider();
        for (int x = startX; x <= sectionMaxX - sectionMinX; x++) {
            for (int z = startZ; z <= sectionMaxZ - sectionMinZ; z++) {
                final Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, true);
                final ChunkSection[] array = chunk.getSectionArray();
                for (int i = 0; i < array.length; i++) {
                    final ChunkSectionExtensions extensions = (ChunkSectionExtensions) array[i];
                    if (i < sectionMaxY - sectionMinY + 1) {
                        final BattleEnvironmentSection.Initial section = initial.sections().get(BattleEnvironmentImpl.Initial.toSectionIndex(x, sectionMaxZ - sectionMinZ + 1, i, sectionMaxY - sectionMinY + 1, z));
                        extensions.tbcex$setBlockContainer(section.blockStateContainer().copy());
                        extensions.tbcex$setBiomeContainer(section.biomeContainer());
                    } else {
                        extensions.tbcex$clearBlockContainer();
                        extensions.tbcex$clearBiomeContainer(biomeRegistry);
                    }
                    provider.checkBlock(new BlockPos((x << 4) + 1, (world.sectionIndexToCoord(i) << 4) + 1, (z << 4) + 1));
                }
            }
        }
    }

    public static void unApply(final ServerWorld world, final BlockPos start, final int sectionsX, final int sectionsZ) {
        final int sectionMinX = start.getX() >> 4;
        final int sectionMinZ = start.getZ() >> 4;
        final Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);
        for (int x = sectionMinX; x < sectionMinX + sectionsX; x++) {
            for (int z = sectionMinZ; z < sectionMinZ + sectionsZ; z++) {
                final Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }
                final ChunkSection[] array = chunk.getSectionArray();
                for (final ChunkSection section : array) {
                    final ChunkSectionExtensions extensions = (ChunkSectionExtensions) section;
                    extensions.tbcex$clearBlockContainer();
                    extensions.tbcex$clearBiomeContainer(biomeRegistry);
                }
            }
        }
    }

    @Override
    public void onInitialize() {
        BattlePlayerComponentEvent.EVENT.register((entity, builder) -> {
            builder.addComponent(TrackedEntityDataComponent.INSTANCE);
            builder.addComponent(new PlayerControlledBattleEntityComponent(entity.getUuid(), entity.world.getRegistryKey(), entity.getBlockPos()));
        });
        ServerPlayConnectionEvents.INIT.register((handler, server) -> TBCExV3PlayNetworkingInit.register(handler));
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> ((TBCExPlayerEntity) newPlayer).tbcex$setCurrentBattle(((TBCExPlayerEntity) oldPlayer).tbcex$getCurrentBattle()));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            final BattleHandle handle = ((TBCExPlayerEntity) handler.getPlayer()).tbcex$getCurrentBattle();
            if (handle != null) {
                server.execute(() -> {
                    final Battle battle = ((ServerBattleWorld) server.getWorld(handle.getWorldKey())).tryGetBattle(handle);
                    if (battle != null) {
                        battle.pushAction(new BattleParticipantLeaveBattleAction(BattleParticipantHandle.of(handler.player.getUuid(), handle)));
                    }
                });
            }
        });
        CoreBattleActions.init();
        CoreBattleEffects.init();
        CoreBattleParticipantEffects.init();
        CoreBattleEntityComponents.init();
        CoreBattleParticipantStats.init();
        BattleListenerEvent.EVENT.register((view, world) -> view.getState().getEventMap().getEventView(CoreBattleEvents.POST_BATTLE_PARTICIPANT_LEAVE_EVENT).registerListener((state, battleStateView, reason, tracer) -> {
            final Iterator<? extends BattleEntityComponent> iterator = state.entityComponents();
            while (iterator.hasNext()) {
                iterator.next().onLeave(view, world);
            }
        }));
        BattleState.BATTLE_EVENT_INITIALIZATION_EVENT.register(builder -> {
            builder.unsorted(CoreBattleEvents.PRE_BATTLE_SET_BOUNDS_EVENT, PreBattleSetBoundsEvent::convert, PreBattleSetBoundsEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_SET_BOUNDS_EVENT, PostBattleBoundsSetEvent::convert, PostBattleBoundsSetEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_JOIN_EVENT, PreBattleParticipantJoinEvent::convert, PreBattleParticipantJoinEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_PARTICIPANT_JOIN_EVENT, PostBattleParticipantJoinEvent::convert, PostBattleParticipantJoinEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_LEAVE_EVENT, PreBattleParticipantLeaveEvent::convert, PreBattleParticipantLeaveEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_PARTICIPANT_LEAVE_EVENT, PostBattleParticipantLeaveEvent::convert, PostBattleParticipantLeaveEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_BATTLE_END_EVENT, PreBattleEndEvent::convert, PreBattleEndEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_END_EVENT, PostBattleEndEvent::convert, PostBattleEndEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_TEAM_RELATION_CHANGE_EVENT, PreChangeTeamRelationEvent::convert, PreChangeTeamRelationEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_TEAM_RELATION_CHANGE_EVENT, PostChangeTeamRelationEvent::convert, PostChangeTeamRelationEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_BLOCK_STATE_SET_EVENT, PreBlockStateSetEvent::convert, PreBlockStateSetEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BLOCK_STATE_SET_EVENT, PostBlockStateSetEvent::convert, PostBlockStateSetEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_BATTLE_BLOCK_SET_EVENT, PreBattleBlockSetEvent::convert, PreBattleBlockSetEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_BLOCK_SET_EVENT, PostBattleBlockSetEvent::convert, PostBattleBlockSetEvent::invoker);
        });
        BattleParticipantState.BATTLE_PARTICIPANT_EVENT_INITIALIZATION_EVENT.register(builder -> {
            builder.unsorted(CoreBattleParticipantEvents.PRE_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT, PreGiveBattleParticipantItemEvent::convert, PreGiveBattleParticipantItemEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT, PostGiveBattleParticipantItemEvent::convert, PostGiveBattleParticipantItemEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.PRE_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT, PreTakeBattleParticipantItemEvent::convert, PreTakeBattleParticipantItemEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT, PostTakeBattleParticipantItemEvent::convert, PostTakeBattleParticipantItemEvent::invoker);

            builder.unsorted(CoreBattleParticipantEvents.PRE_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT, PreEquipBattleParticipantEquipmentEvent::convert, PreEquipBattleParticipantEquipmentEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT, PostEquipBattleParticipantEquipmentEvent::convert, PostEquipBattleParticipantEquipmentEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.PRE_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT, PreUnequipBattleParticipantEquipmentEvent::convert, PreUnequipBattleParticipantEquipmentEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT, PostUnequipBattleParticipantEquipmentEvent::convert, PostUnequipBattleParticipantEquipmentEvent::invoker);

            builder.unsorted(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_TEAM_EVENT, PreBattleParticipantSetTeamEvent::convert, PreBattleParticipantSetTeamEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_TEAM_EVENT, PostBattleParticipantSetTeamEvent::convert, PostBattleParticipantSetTeamEvent::invoker);

            builder.unsorted(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT, PreBattleParticipantSetBoundsEvent::convert, PreBattleParticipantSetBoundsEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT, PostBattleParticipantSetBoundsEvent::convert, PostBattleParticipantSetBoundsEvent::invoker);

            builder.sorted(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_HEALTH_EVENT, PreBattleParticipantSetHealthEvent::convert, PreBattleParticipantSetHealthEvent::invoker, Comparator.comparing(PreBattleParticipantSetHealthEvent::phase, PreBattleParticipantSetHealthEvent.PHASE_TRACKER.phaseComparator()));
            builder.unsorted(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_HEALTH_EVENT, PostBattleParticipantSetHealthEvent::convert, PostBattleParticipantSetHealthEvent::invoker);

            builder.unsorted(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_DEATH_EVENT, PreBattleParticipantDeathEvent::convert, PreBattleParticipantDeathEvent::invoker);
        });
    }

    public static void setClientLogger(final Supplier<@Nullable Logger> logger) {
        CLIENT_LOGGER = logger;
    }

    public static Logger getLogger() {
        if (CLIENT_LOGGER == null) {
            return LOGGER;
        } else {
            final Logger logger = CLIENT_LOGGER.get();
            return logger == null ? LOGGER : logger;
        }
    }

    public static Identifier createId(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
