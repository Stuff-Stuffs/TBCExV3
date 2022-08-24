package io.github.stuff_stuffs.tbcexv3gui.api.widget;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix4f;

public interface WidgetRenderContext {
    float time();

    WidgetRenderContext pushMatrix(Matrix4f matrix);

    WidgetRenderContext pushScissor(Rectangle scissor);

    VertexConsumer getVertexConsumer(RenderLayer renderLayer);
}
