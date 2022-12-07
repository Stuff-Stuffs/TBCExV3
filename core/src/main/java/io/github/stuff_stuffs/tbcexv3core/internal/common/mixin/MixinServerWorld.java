package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.internal.common.AbstractServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.github.stuff_stuffs.tbcexv3core.internal.common.world.ServerBattleWorldContainer;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World implements AbstractServerBattleWorld {
    private ServerBattleWorldContainer battleWorldContainer;

    protected MixinServerWorld(final MutableWorldProperties properties, final RegistryKey<World> registryRef, final RegistryEntry<DimensionType> dimension, final Supplier<Profiler> profiler, final boolean isClient, final boolean debugWorld, final long seed, final int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initialize(final MinecraftServer server, final Executor workerExecutor, final LevelStorage.Session session, final ServerWorldProperties properties, final RegistryKey worldKey, final DimensionOptions dimensionOptions, final WorldGenerationProgressListener worldGenerationProgressListener, final boolean debugWorld, final long seed, final List spawners, final boolean shouldTickTime, final CallbackInfo ci) {
        battleWorldContainer = new ServerBattleWorldContainer(worldKey, session.getDirectory(TBCExV3Core.TBCEX_WORLD_SAVE_PATH));
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void closeHook(final CallbackInfo ci) {
        battleWorldContainer.close();
    }

    @Override
    public @Nullable BattleView tryGetBattleView(final @NotNull BattleHandle handle) {
        if (!handle.getWorldKey().equals(getRegistryKey())) {
            return null;
        }
        return battleWorldContainer.getBattle(handle.getUuid());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickInject(final BooleanSupplier shouldKeepTicking, final CallbackInfo ci) {
        battleWorldContainer.tick();
    }

    @Override
    public @Nullable Battle tryGetBattle(final BattleHandle handle) {
        return battleWorldContainer.getBattle(handle.getUuid());
    }

    @Override
    public BattleHandle createBattle(final Map<BattleEntity, Identifier> entities, final InitialTeamSetupBattleAction teamSetupAction) {
        return battleWorldContainer.createBattle(entities, teamSetupAction);
    }

    @Override
    public List<BattleParticipantHandle> getBattles(final UUID entityUuid, final TriState active) {
        return battleWorldContainer.getBattles(entityUuid, active);
    }

    @Override
    public void pushDelayedPlayerComponent(final UUID uuid, final BattleHandle handle, final BattleEntityComponent component) {
        battleWorldContainer.pushDelayedPlayerComponent(uuid, handle, component);
    }

    @Override
    public boolean tryApplyDelayedComponents(final UUID uuid, final ServerWorld world) {
        return battleWorldContainer.delayedComponent(uuid, world);
    }
}
