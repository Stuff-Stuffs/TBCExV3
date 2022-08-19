package io.github.stuff_stuffs.tbcexv3gui.internal.client;

import io.github.stuff_stuffs.tbcexv3gui.internal.mixin.AccessorMultiPhaseParameters;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;

import java.util.Map;

public final class TBCExV3GuiRenderLayers extends RenderPhase {
    private static final RenderPhase.Target GUI_TARGET = new RenderPhase.Target("tbcexv3gui_target", () -> TBCExV3GuiClient.getGuiFrameBuffer().beginWrite(false), () -> MinecraftClient.getInstance().getFramebuffer().beginWrite(false));
    private static final Map<RenderLayer, RenderLayer> GUI_RENDER_LAYERS = new Reference2ObjectOpenHashMap<>();
    public static final RenderLayer POS_COLOR_NO_CULL = RenderLayer.of("guiRenderLayer_no_cull", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, false, false, RenderLayer.MultiPhaseParameters.builder().target(GUI_TARGET).writeMaskState(RenderPhase.ALL_MASK).shader(new Shader(GameRenderer::getPositionColorShader)).depthTest(RenderPhase.ALWAYS_DEPTH_TEST).transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY).cull(RenderPhase.DISABLE_CULLING).build(false));

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
            } else if (target == RenderPhase.MAIN_TARGET || target == RenderPhase.ITEM_TARGET || target == RenderPhase.PARTICLES_TARGET || target == RenderPhase.TRANSLUCENT_TARGET || target == RenderPhase.WEATHER_TARGET) {
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
