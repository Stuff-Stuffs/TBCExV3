package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface MixinGameRenderer {
    @Invoker
    double invokeGetFov(Camera camera, float tickDelta, boolean b);
}
