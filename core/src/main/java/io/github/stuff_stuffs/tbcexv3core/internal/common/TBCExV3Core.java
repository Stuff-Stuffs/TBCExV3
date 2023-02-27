package io.github.stuff_stuffs.tbcexv3core.internal.common;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleListenerEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleParticipantLeaveBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.CoreBattleActions;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.CoreBattleEffects;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.CoreBattleParticipantEffects;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.CoreBattleParticipantEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health.PreBattleParticipantSetHealthEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.CoreBattleParticipantStats;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.*;
import io.github.stuff_stuffs.tbcexv3core.api.util.EventGenerationUtil;
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
            builder.unsortedBooleanAnd(CoreBattleEvents.PRE_BATTLE_SET_BOUNDS_EVENT);
            builder.unsortedViewLike(CoreBattleEvents.POST_BATTLE_SET_BOUNDS_EVENT);

            builder.unsortedBooleanAnd(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_JOIN_EVENT);
            builder.unsortedViewLike(CoreBattleEvents.POST_BATTLE_PARTICIPANT_JOIN_EVENT);

            builder.unsortedBooleanAnd(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_LEAVE_EVENT);
            builder.unsortedViewLike(CoreBattleEvents.POST_BATTLE_PARTICIPANT_LEAVE_EVENT);

            builder.unsortedViewLike(CoreBattleEvents.PRE_BATTLE_END_EVENT);
            builder.unsortedViewLike(CoreBattleEvents.POST_BATTLE_END_EVENT);

            builder.unsortedBooleanAnd(CoreBattleEvents.PRE_TEAM_RELATION_CHANGE_EVENT);
            builder.unsortedViewLike(CoreBattleEvents.POST_TEAM_RELATION_CHANGE_EVENT);

            builder.unsortedBooleanAnd(CoreBattleEvents.PRE_BLOCK_STATE_SET_EVENT);
            builder.unsortedViewLike(CoreBattleEvents.POST_BLOCK_STATE_SET_EVENT);

            builder.unsortedBooleanAnd(CoreBattleEvents.PRE_BATTLE_BLOCK_SET_EVENT);
            builder.unsortedViewLike(CoreBattleEvents.POST_BATTLE_BLOCK_SET_EVENT);
        });
        BattleParticipantState.BATTLE_PARTICIPANT_EVENT_INITIALIZATION_EVENT.register(builder -> {
            builder.unsortedBooleanAnd(CoreBattleParticipantEvents.PRE_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT);
            builder.unsortedViewLike(CoreBattleParticipantEvents.POST_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT);
            builder.unsortedBooleanAnd(CoreBattleParticipantEvents.PRE_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT);
            builder.unsortedViewLike(CoreBattleParticipantEvents.POST_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT);

            builder.unsortedBooleanAnd(CoreBattleParticipantEvents.PRE_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT);
            builder.unsortedViewLike(CoreBattleParticipantEvents.POST_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT);
            builder.unsortedBooleanAnd(CoreBattleParticipantEvents.PRE_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT);
            builder.unsortedViewLike(CoreBattleParticipantEvents.POST_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT);

            builder.unsortedBooleanAnd(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_TEAM_EVENT);
            builder.unsortedViewLike(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_TEAM_EVENT);

            builder.unsortedBooleanAnd(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT);
            builder.unsortedViewLike(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT);

            builder.sorted(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_HEALTH_EVENT, EventGenerationUtil.generateDoubleConverter(PreBattleParticipantSetHealthEvent.class, PreBattleParticipantSetHealthEvent.View.class, i -> i, 1), EventGenerationUtil.generateDoubleReuseInvoker(PreBattleParticipantSetHealthEvent.class, 1), Comparator.comparing(PreBattleParticipantSetHealthEvent::phase, PreBattleParticipantSetHealthEvent.PHASE_TRACKER.phaseComparator()));
            builder.unsortedViewLike(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_HEALTH_EVENT);

            builder.unsortedViewLike(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_DEATH_EVENT);
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
