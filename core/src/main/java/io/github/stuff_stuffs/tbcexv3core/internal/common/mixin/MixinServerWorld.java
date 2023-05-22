package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.AbstractServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.PlayerCurrentBattleSender;
import io.github.stuff_stuffs.tbcexv3core.internal.common.world.BattleDisplayWorld;
import io.github.stuff_stuffs.tbcexv3core.internal.common.world.BattleDisplayWorldContainer;
import io.github.stuff_stuffs.tbcexv3core.internal.common.world.ServerBattleWorldContainer;
import io.github.stuff_stuffs.tbcexv3core.internal.common.world.WorldBorderExtensions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World implements AbstractServerBattleWorld, BattleDisplayWorld {
    @Unique
    private ServerBattleWorldContainer tbcex$battleWorldContainer;
    @Unique
    private BattleDisplayWorldContainer tbcex$battleDisplayContainer;

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, profiler, isClient, debugWorld, biomeAccess, maxChainedNeighborUpdates);
    }

    @Inject(method = "shouldTick(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void stopEntityTick(final BlockPos pos, final CallbackInfoReturnable<Boolean> cir) {
        if (tbcex$battleDisplayContainer != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldTickEntity", at = @At("HEAD"), cancellable = true)
    private void stopEntityTick0(final BlockPos pos, final CallbackInfoReturnable<Boolean> cir) {
        if (tbcex$battleDisplayContainer != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "shouldTickBlocksInChunk", at = @At("HEAD"), cancellable = true)
    private void stopBlockTick(final long chunkPos, final CallbackInfoReturnable<Boolean> cir) {
        if (tbcex$battleDisplayContainer != null) {
            cir.setReturnValue(false);
        }
    }


    @Inject(method = "<init>", at = @At("RETURN"))
    private void initialize(final MinecraftServer server, final Executor workerExecutor, final LevelStorage.Session session, final ServerWorldProperties properties, final RegistryKey<World> worldKey, final DimensionOptions dimensionOptions, final WorldGenerationProgressListener worldGenerationProgressListener, final boolean debugWorld, final long seed, final List spawners, final boolean shouldTickTime, final CallbackInfo ci) {
        tbcex$battleWorldContainer = new ServerBattleWorldContainer((ServerWorld) (Object) this, worldKey, session.getDirectory(TBCExV3Core.TBCEX_WORLD_SAVE_PATH), server.getThread());
        if (worldKey.equals(BattleDisplayWorld.BATTLE_DISPLAY_WORLD)) {
            final WorldBorder border = getWorldBorder();
            ((WorldBorderExtensions) border).freeze();
            tbcex$battleDisplayContainer = new BattleDisplayWorldContainer((ServerWorld) (Object) this, border.getMaxRadius(), (int) border.getCenterX(), getBottomY(), (int) border.getCenterZ());
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void closeHook(final CallbackInfo ci) {
        tbcex$battleWorldContainer.close();
    }

    @Override
    public @Nullable BattleView tryGetBattleView(final @NotNull BattleHandle handle) {
        if (!handle.getWorldKey().equals(getRegistryKey())) {
            return null;
        }
        return tbcex$battleWorldContainer.getBattle(handle.getUuid());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickInject(final BooleanSupplier shouldKeepTicking, final CallbackInfo ci) {
        tbcex$battleWorldContainer.tick();
    }

    @Override
    public @Nullable Battle tryGetBattle(final BattleHandle handle) {
        return tbcex$battleWorldContainer.getBattle(handle.getUuid());
    }

    @Override
    public BattleHandle createBattle(final Map<BattleEntity, Identifier> entities, final InitialTeamSetupBattleAction teamSetupAction) {
        return tbcex$battleWorldContainer.createBattle(entities, teamSetupAction);
    }

    @Override
    public BattleHandle createBattle(final Map<BattleEntity, Identifier> entities, final InitialTeamSetupBattleAction teamSetupAction, final BattleBounds bounds, final int padding) {
        return tbcex$battleWorldContainer.createBattle(entities, teamSetupAction, bounds, padding);
    }

    @Override
    public List<BattleParticipantHandle> getBattles(final UUID entityUuid, final TriState active) {
        return tbcex$battleWorldContainer.getBattles(entityUuid, active);
    }

    @Override
    public void pushDelayedPlayerComponent(final UUID uuid, final BattleHandle handle, final BattleEntityComponent component) {
        tbcex$battleWorldContainer.pushDelayedPlayerComponent(uuid, handle, component);
    }

    @Override
    public boolean tryApplyDelayedComponents(final UUID uuid, final ServerWorld world) {
        return tbcex$battleWorldContainer.delayedComponent(uuid, world);
    }

    @Override
    public boolean tbcex$isBattleDisplayWorld() {
        return tbcex$battleDisplayContainer != null;
    }

    @Override
    public BlockPos tbccex$allocate(final BattleHandle handle, final int maxWidth) {
        if (!tbcex$isBattleDisplayWorld()) {
            throw new RuntimeException("Cannot allocate battle space in non-battle display world!");
        }
        return tbcex$battleDisplayContainer.allocate(handle, maxWidth);
    }

    @Override
    public void tbcex$deallocate(final BattleHandle handle) {
        if (!tbcex$isBattleDisplayWorld()) {
            throw new RuntimeException("Cannot deallocate battle space in non-battle display world!");
        }
        tbcex$battleDisplayContainer.deallocate(handle);
    }

    @Override
    public void tbcex$apply(final BlockPos start, final BattleEnvironmentImpl.Initial environment) {
        if (!tbcex$isBattleDisplayWorld()) {
            throw new RuntimeException("Cannot deallocate battle space in non-battle display world!");
        }
        TBCExV3Core.applyInitialToWorld((ServerWorld) (Object) this, environment, start);
    }

    @Inject(method = "addPlayer", at = @At("RETURN"))
    private void updateBattleStateHook(final ServerPlayerEntity player, final CallbackInfo ci) {
        PlayerCurrentBattleSender.send(player, ((TBCExPlayerEntity) player).tbcex$getCurrentBattle());
    }
}
