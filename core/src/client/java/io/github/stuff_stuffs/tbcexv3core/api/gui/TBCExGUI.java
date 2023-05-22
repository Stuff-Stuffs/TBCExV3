package io.github.stuff_stuffs.tbcexv3core.api.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.*;

public final class TBCExGUI {
    public static final Color DEFAULT = new Color(0.25F, 0.25F, 0.25F, 0.25F);
    public static final Color DARK = new Color(0.35F, 0.35F, 0.35F, 0.65F);
    public static final Color LIGHT = new Color(0.4F, 0.4F, 0.4F, 0.65F);
    public static final Color SELECTED = new Color(0.425F, 0.425F, 0.425F, 0.90F);

    public static final Surface DEFAULT_SURFACE = Surface.flat(DEFAULT.argb());
    public static final Surface DARK_SURFACE = Surface.flat(DARK.argb());
    public static final Surface LIGHT_SURFACE = Surface.flat(LIGHT.argb());
    public static final Surface SELECTED_SURFACE = Surface.flat(SELECTED.argb());

    public static final Surface TOOLTIP_SURFACE = (matrices, component) -> {
        final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        TooltipBackgroundRenderer.render((matrix, buffer1, startX, startY, endX, endY, z, startColor, endColor) -> {
            buffer1.vertex(matrix, endX, startY, z).color(startColor).next();
            buffer1.vertex(matrix, startX, startY, z).color(startColor).next();
            buffer1.vertex(matrix, startX, endY, z).color(endColor).next();
            buffer1.vertex(matrix, endX, endY, z).color(endColor).next();
        }, matrices.peek().getPositionMatrix(), buffer, component.x() + 2, component.y() + 2, component.width() - 4, component.height() - 4, component.zIndex());
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.disableBlend();
    };

    private TBCExGUI() {
    }
}
