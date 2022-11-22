package io.github.stuff_stuffs.tbcexv3_gui.internal.client.mixin;

import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@Mixin(RenderPhase.TextureBase.class)
public interface AccessorTextureBase {
    @Invoker(value = "getId")
    Optional<Identifier> getId();
}
