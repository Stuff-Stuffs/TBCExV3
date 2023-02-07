package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.client.world.ClientBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.internal.client.world.ClientBattleWorldContainer;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld extends World implements BattleWorld, ClientBattleWorld {
    private ClientBattleWorldContainer battleWorldContainer;

    protected MixinClientWorld(final MutableWorldProperties properties, final RegistryKey<World> registryRef, final RegistryEntry<DimensionType> dimension, final Supplier<Profiler> profiler, final boolean isClient, final boolean debugWorld, final long seed, final int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initialize(final ClientPlayNetworkHandler networkHandler, final ClientWorld.Properties properties, final RegistryKey<World> registryRef, final RegistryEntry dimensionTypeEntry, final int loadDistance, final int simulationDistance, final Supplier profiler, final WorldRenderer worldRenderer, final boolean debugWorld, final long seed, final CallbackInfo ci) {
        battleWorldContainer = new ClientBattleWorldContainer();
    }

    @Override
    public @Nullable BattleView tryGetBattleView(final BattleHandle handle) {
        return battleWorldContainer.getBattle(handle);
    }

    @Override
    public void update(final BattleUpdate update) {
        battleWorldContainer.update(update);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(final BooleanSupplier shouldKeepTicking, final CallbackInfo ci) {
        battleWorldContainer.tick();
    }

    @Override
    public List<BattleParticipantHandle> getBattles(final UUID entityUuid, final TriState active) {
        return battleWorldContainer.getEntityBattles(entityUuid, active);
    }

    @Override
    public void update(final UUID entityId, final List<BattleParticipantHandle> battleIds, final List<BattleParticipantHandle> inactiveBattles) {
        battleWorldContainer.update(entityId, battleIds, inactiveBattles);
    }
}
