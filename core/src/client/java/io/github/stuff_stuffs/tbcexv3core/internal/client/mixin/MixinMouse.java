package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import io.github.stuff_stuffs.tbcexv3core.internal.client.screen.MouseUnlockingScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MixinMouse {
    @Redirect(method = "lockCursor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"))
    private void redirectHook(final MinecraftClient instance, final Screen screen) {
        if (screen != null || (!(instance.currentScreen instanceof MouseUnlockingScreen s) || !s.skipCloseFromCursorUnlock())) {
            instance.setScreen(null);
        }
    }
}
