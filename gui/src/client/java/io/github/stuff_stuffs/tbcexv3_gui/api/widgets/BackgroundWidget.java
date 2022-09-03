package io.github.stuff_stuffs.tbcexv3_gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container.AbstractSingleChildWidget;

import java.util.function.ToDoubleFunction;

public final class BackgroundWidget<T> extends AbstractSingleChildWidget<T> {
    private final ToDoubleFunction<? super T> outlineWidth;
    private final WidgetRenderUtils.Renderer<? super T> renderer;
    private Rectangle bounds;

    public BackgroundWidget(final ToDoubleFunction<? super T> outlineWidth, final WidgetRenderUtils.Renderer<? super T> renderer) {
        this.outlineWidth = outlineWidth;
        this.renderer = renderer;
    }

    @Override
    public Rectangle resize(final RectangleRange range) {
        return bounds = super.resize(range);
    }

    @Override
    protected void drawSelf(final WidgetRenderContext context) {
        if (getChild() != null && bounds != null) {
            final WidgetContext<T> widgetContext = getWidgetContext();
            if (widgetContext == null) {
                throw new NullPointerException();
            }
            final Rectangle rect = bounds.expand(outlineWidth.applyAsDouble(widgetContext.getData()));
            renderer.render(widgetContext.getData(), context, rect);
        }
    }

}
