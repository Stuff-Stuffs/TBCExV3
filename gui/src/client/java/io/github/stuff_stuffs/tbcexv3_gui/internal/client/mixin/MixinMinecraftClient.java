package io.github.stuff_stuffs.tbcexv3_gui.internal.client.mixin;

import io.github.stuff_stuffs.tbcexv3_gui.internal.client.TBCExV3GuiClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    private void resolutionChanged(final CallbackInfo ci) {
        final MinecraftClient instance = MinecraftClient.getInstance();
        TBCExV3GuiClient.RESOLUTION_CHANGED_EVENT.invoker().resolutionChanged(instance.getWindow().getFramebufferWidth(), instance.getWindow().getFramebufferHeight());
    }
}
