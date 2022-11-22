package io.github.stuff_stuffs.tbcexv3_gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3_gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

public class TerminalWidget<T> implements Widget<T> {
    private final StateUpdater<T> stateUpdater;
    private final WidgetRenderUtils.Renderer<? super T> renderer;
    private final Sizer<? super T> sizer;
    private WidgetContext<T> context;
    private Rectangle bounds;

    public TerminalWidget(final StateUpdater<T> stateUpdater, final WidgetRenderUtils.Renderer<? super T> renderer, final Sizer<? super T> sizer) {
        this.stateUpdater = stateUpdater;
        this.renderer = renderer;
        this.sizer = sizer;
    }

    @Override
    public void setup(final WidgetContext<T> context) {
        this.context = context;
    }

    @Override
    public boolean handleEvent(final WidgetEvent event) {
        return stateUpdater.event(event, context.getData());
    }

    @Override
    public Rectangle resize(final RectangleRange range) {
        final Rectangle bounds = sizer.calculateSize(context.getData(), range);
        stateUpdater.updateBounds(bounds, context.getData());
        return this.bounds = bounds;
    }

    @Override
    public void draw(final WidgetRenderContext context) {
        renderer.render(this.context.getData(), context, bounds);
    }

    @Override
    public void postDraw(final MatrixStack stack, final VertexConsumerProvider vertexConsumers, Rectangle screenBounds) {
        renderer.postDraw(context.getData(), stack, vertexConsumers);
    }
}
