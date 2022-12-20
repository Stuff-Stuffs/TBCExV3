package io.github.stuff_stuffs.tbcexv3_gui.internal.client.mixin;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderLayer.MultiPhaseParameters.class)
public interface AccessorMultiPhaseParameters {
    @Accessor
    RenderPhase.TextureBase getTexture();

    @Accessor
    RenderPhase.ShaderProgram getProgram();

    @Accessor
    RenderPhase.Transparency getTransparency();

    @Accessor
    RenderPhase.DepthTest getDepthTest();

    @Accessor
    RenderPhase.Cull getCull();

    @Accessor
    RenderPhase.Lightmap getLightmap();

    @Accessor
    RenderPhase.Overlay getOverlay();

    @Accessor
    RenderPhase.Layering getLayering();

    @Accessor
    RenderPhase.Target getTarget();

    @Accessor
    RenderPhase.Texturing getTexturing();

    @Accessor
    RenderPhase.WriteMaskState getWriteMaskState();

    @Accessor
    RenderPhase.LineWidth getLineWidth();

    @Accessor
    RenderLayer.OutlineMode getOutlineMode();
}

