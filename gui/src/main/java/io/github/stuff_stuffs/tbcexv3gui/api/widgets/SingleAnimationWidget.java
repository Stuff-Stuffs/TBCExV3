package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;

import java.util.Optional;

public class SingleAnimationWidget<T> extends AbstractSingleChildWidget<T> {
    private final Animation<? super T> animation;

    public SingleAnimationWidget(final Animation<? super T> animation) {
        this.animation = animation;
    }

    @Override
    public void draw(final WidgetRenderContext context) {
        final WidgetInfo<T, ?> child = getChild();
        if (child == null) {
            throw new NullPointerException();
        }
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        final Optional<WidgetRenderContext> transformed = animation.animate(widgetContext.getData(), context);
        transformed.ifPresent(child.widget::draw);
    }

    @Override
    public boolean handleEvent(final WidgetEvent event) {
        final WidgetContext<T> context = getWidgetContext();
        if (context == null) {
            throw new NullPointerException();
        }
        final Optional<WidgetEvent> transformed = animation.animateEvent(context.getData(), event);
        return transformed.isPresent() && super.handleEvent(transformed.get());
    }

    @Override
    protected void drawSelf(final WidgetRenderContext context) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        animation.animateSelf(widgetContext.getData(), context);
    }

    public interface Animation<T> {
        Optional<WidgetRenderContext> animate(T data, WidgetRenderContext parent);

        default void animateSelf(final T data, final WidgetRenderContext context) {
        }

        Optional<WidgetEvent> animateEvent(T data, WidgetEvent event);
    }
}
