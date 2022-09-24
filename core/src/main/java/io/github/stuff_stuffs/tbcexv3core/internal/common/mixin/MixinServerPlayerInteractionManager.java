package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerInteractionManager.class)
public class MixinServerPlayerInteractionManager {

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Inject(method = "setGameMode", cancellable = true, at = @At("HEAD"))
    private void setGameModeHook(final GameMode gameMode, final GameMode previousGameMode, final CallbackInfo ci) {
        final TBCExPlayerEntity tbcexPlayer = (TBCExPlayerEntity) player;
        if (tbcexPlayer.tbcex$getCurrentBattle() != null && player.getWorld().getRegistryKey().equals(tbcexPlayer.tbcex$getCurrentBattle().getWorldKey()) && gameMode != GameMode.SPECTATOR) {
            ci.cancel();
        }
    }
}
