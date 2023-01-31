package io.github.stuff_stuffs.tbcexv3core.api.util;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

public final class TBCExDrawer {
    private TBCExDrawer() {
    }

    public static void drawTrapezoid(final MatrixStack matrices, final Trapezoid trapezoid, final Color innerColor, final Color outerColor, final double x, final double y) {
        final var buffer = Tessellator.getInstance().getBuffer();
        final var matrix = matrices.peek().getPositionMatrix();
        final int inColor = innerColor.argb();
        final int outColor = outerColor.argb();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, (float) (x + trapezoid.x(0)), (float) (y + trapezoid.y(0)), 0).color(inColor).next();
        buffer.vertex(matrix, (float) (x + trapezoid.x(1)), (float) (y + trapezoid.y(1)), 0).color(outColor).next();
        buffer.vertex(matrix, (float) (x + trapezoid.x(2)), (float) (y + trapezoid.y(2)), 0).color(outColor).next();
        buffer.vertex(matrix, (float) (x + trapezoid.x(3)), (float) (y + trapezoid.y(3)), 0).color(inColor).next();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator.getInstance().draw();
        RenderSystem.enableCull();
    }
}

