package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Quadrilateral;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface WidgetRenderContext {
    float time();

    WidgetRenderContext pushMatrix(Matrix4f matrix);

    WidgetRenderContext pushScissor(Quadrilateral scissor);

    VertexConsumer getVertexConsumer(RenderLayer renderLayer);
}
