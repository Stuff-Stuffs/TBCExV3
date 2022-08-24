package io.github.stuff_stuffs.tbcexv3gui.impl.widget;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix4f;

class WrappedWidgetRenderContextImpl implements WidgetRenderContext {
    private final WidgetRenderContextImpl parent;
    private final int drawState;

    public WrappedWidgetRenderContextImpl(final WidgetRenderContextImpl parent, final int drawState) {
        this.parent = parent;
        this.drawState = drawState;
    }

    @Override
    public float time() {
        return parent.time();
    }

    @Override
    public WidgetRenderContext pushMatrix(final Matrix4f matrix) {
        return parent.pushMatrix(matrix, drawState);
    }

    @Override
    public WidgetRenderContext pushScissor(final Rectangle scissor) {
        return parent.pushScissor(scissor, drawState);
    }

    @Override
    public VertexConsumer getVertexConsumer(final RenderLayer renderLayer) {
        if (!renderLayer.areVerticesNotShared()) {
            throw new IllegalArgumentException();
        }
        return new WidgetVertexConsumer(renderLayer, parent.getMatrix(drawState), (layer, data) -> parent.submit(layer, data, drawState));
    }
}
