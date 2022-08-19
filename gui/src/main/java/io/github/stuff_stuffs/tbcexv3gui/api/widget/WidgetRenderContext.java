package io.github.stuff_stuffs.tbcexv3gui.api.widget;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix4f;

public interface WidgetRenderContext {
    float time();

    void pushMatrix(Matrix4f matrix);

    void popMatrix();

    void pushScissor(Rectangle scissor);

    void popScissor();

    VertexConsumer getVertexConsumer(RenderLayer renderLayer);
}
