package io.github.stuff_stuffs.tbcexv3_gui.internal.client;

import io.github.stuff_stuffs.tbcexv3_gui.internal.client.mixin.AccessorMultiPhaseParameters;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Map;
import java.util.function.Function;

public final class TBCExV3GuiRenderLayers extends RenderPhase {
    private static final RenderPhase.Target GUI_TARGET = new RenderPhase.Target("tbcexv3gui_target", () -> TBCExV3GuiClient.getGuiFrameBuffer().beginWrite(false), () -> MinecraftClient.getInstance().getFramebuffer().beginWrite(false));
    private static final Map<RenderLayer, RenderLayer> GUI_RENDER_LAYERS = new Reference2ObjectOpenHashMap<>();
    private static final RenderLayer POS_COLOR_NO_CULL = RenderLayer.of("guiRenderLayer_no_cull", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 4096, false, false, RenderLayer.MultiPhaseParameters.builder().target(GUI_TARGET).writeMaskState(ALL_MASK).shader(new Shader(GameRenderer::getPositionColorShader)).depthTest(LEQUAL_DEPTH_TEST).transparency(TRANSLUCENT_TRANSPARENCY).cull(DISABLE_CULLING).build(false));
    private static final RenderLayer POS_COLOR_CULL = RenderLayer.of("guiRenderLayer_no_cull", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 4096, false, false, RenderLayer.MultiPhaseParameters.builder().target(GUI_TARGET).writeMaskState(ALL_MASK).shader(new Shader(GameRenderer::getPositionColorShader)).depthTest(LEQUAL_DEPTH_TEST).transparency(TRANSLUCENT_TRANSPARENCY).cull(ENABLE_CULLING).build(false));
    private static final Function<Identifier, RenderLayer> POS_COLOR_TEXTURE = Util.memoize(identifier -> RenderLayer.of("guiRenderLayer_texture:" + identifier, VertexFormats.POSITION_COLOR_TEXTURE, VertexFormat.DrawMode.QUADS, 4096, false, false, RenderLayer.MultiPhaseParameters.builder().texture(new Texture(identifier, false, false)).target(GUI_TARGET).writeMaskState(ALL_MASK).shader(new Shader(GameRenderer::getPositionColorTexShader)).depthTest(LEQUAL_DEPTH_TEST).transparency(TRANSLUCENT_TRANSPARENCY).cull(ENABLE_CULLING).build(false)));

    public static RenderLayer getPosColorCull() {
        return POS_COLOR_CULL;
    }

    public static RenderLayer getPosColorNoCull() {
        return POS_COLOR_NO_CULL;
    }

    public static RenderLayer getPosColorTexture(final Identifier tex) {
        return POS_COLOR_TEXTURE.apply(tex);
    }

    private TBCExV3GuiRenderLayers(final String name, final Runnable beginAction, final Runnable endAction) {
        super(name, beginAction, endAction);
    }


    public static RenderLayer getGuiRenderLayer(final RenderLayer renderLayer) {
        RenderLayer guiRenderLayer = GUI_RENDER_LAYERS.get(renderLayer);
        if (guiRenderLayer != null) {
            return guiRenderLayer;
        }
        if (renderLayer instanceof RenderLayer.MultiPhase multiPhase) {
            final AccessorMultiPhaseParameters accessor = (AccessorMultiPhaseParameters) (Object) multiPhase.getPhases();
            final RenderPhase.Target target = accessor.getTarget();
            if (target == GUI_TARGET) {
                return renderLayer;
            } else if (target == MAIN_TARGET || target == ITEM_TARGET || target == PARTICLES_TARGET || target == TRANSLUCENT_TARGET || target == WEATHER_TARGET) {
                final RenderLayer.MultiPhaseParameters parameters = RenderLayer.MultiPhaseParameters.builder()
                        .target(GUI_TARGET)
                        .texture(accessor.getTexture())
                        .shader(accessor.getShader())
                        .transparency(accessor.getTransparency())
                        .depthTest(accessor.getDepthTest())
                        .cull(accessor.getCull())
                        .overlay(accessor.getOverlay())
                        .lightmap(accessor.getLightmap())
                        .layering(accessor.getLayering())
                        .texturing(accessor.getTexturing())
                        .writeMaskState(accessor.getWriteMaskState())
                        .lineWidth(accessor.getLineWidth())
                        .build(accessor.getOutlineMode());
                guiRenderLayer = RenderLayer.of("guiRenderLayer[" + renderLayer + "]", renderLayer.getVertexFormat(), renderLayer.getDrawMode(), renderLayer.getExpectedBufferSize(), renderLayer.hasCrumbling(), false, parameters);
                GUI_RENDER_LAYERS.put(renderLayer, guiRenderLayer);
                return guiRenderLayer;
            }
        }
        throw new IllegalArgumentException();
    }
}
