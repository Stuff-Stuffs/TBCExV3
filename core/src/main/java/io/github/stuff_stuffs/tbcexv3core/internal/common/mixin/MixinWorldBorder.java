package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import io.github.stuff_stuffs.tbcexv3core.internal.common.world.WorldBorderExtensions;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
public class MixinWorldBorder implements WorldBorderExtensions {
    @Unique
    private boolean frozen = false;

    @Override
    public void freeze() {
        frozen = true;
    }

    @Inject(method = "setCenter", at = @At("HEAD"), cancellable = true)
    private void cancelSetCenter(final double x, final double z, final CallbackInfo ci) {
        if (frozen) {
            ci.cancel();
        }
    }

    @Inject(method = "setMaxRadius", at = @At("HEAD"), cancellable = true)
    private void cancelSetMaxRadius(final int maxRadius, final CallbackInfo ci) {
        if (frozen) {
            ci.cancel();
        }
    }

    @Inject(method = "setSafeZone", at = @At("HEAD"), cancellable = true)
    private void cancelSetSafeZone(final double safeZone, final CallbackInfo ci) {
        if (frozen) {
            ci.cancel();
        }
    }

    @Inject(method = "interpolateSize", at = @At("HEAD"), cancellable = true)
    private void cancelInterpolateSize(final double fromSize, final double toSize, final long time, final CallbackInfo ci) {
        if (frozen) {
            ci.cancel();
        }
    }

    @Inject(method = "setSize", at = @At("HEAD"), cancellable = true)
    private void cancelSetSize(final double size, final CallbackInfo ci) {
        if (frozen) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void cancelSetMaxRadius(final CallbackInfo ci) {
        if (frozen) {
            ci.cancel();
        }
    }
}
