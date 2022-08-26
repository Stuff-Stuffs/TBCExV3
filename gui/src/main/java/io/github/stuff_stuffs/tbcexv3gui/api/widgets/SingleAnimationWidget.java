package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;

import java.util.Optional;

public class SingleAnimationWidget<T> extends AbstractSingleChildWidget<T> {
    private final Animation<? super T> animation;
    private final StateUpdater<? super T> stateUpdater;
    private final WidgetEventPhase eventPhase;

    public SingleAnimationWidget(final Animation<? super T> animation, final StateUpdater<? super T> stateUpdater, final WidgetEventPhase eventPhase) {
        this.animation = animation;
        this.stateUpdater = stateUpdater;
        this.eventPhase = eventPhase;
    }

    @Override
    public void draw(final WidgetRenderContext context) {
        drawSelf(context);
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
        final WidgetInfo<T, ?> child = getChild();
        if (child == null) {
            throw new NullPointerException();
        }
        final Optional<WidgetEvent> transformed = animation.animateEvent(context.getData(), event);
        if (transformed.isPresent()) {
            if (eventPhase == WidgetEventPhase.PRE_CHILD) {
                return stateUpdater.event(event, context.getData()) || child.widget.handleEvent(event);
            } else {
                return child.widget.handleEvent(event) || stateUpdater.event(event, context.getData());
            }
        } else {
            return false;
        }
    }

    @Override
    protected void drawSelf(final WidgetRenderContext context) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        animation.animateSelf(widgetContext.getData(), context);
    }

    @Override
    public Rectangle resize(final Rectangle min, final Rectangle max) {
        final Rectangle rectangle = super.resize(min, max);
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        stateUpdater.updateBounds(rectangle, widgetContext.getData());
        return rectangle;
    }

    public interface Animation<T> {
        Optional<WidgetRenderContext> animate(T data, WidgetRenderContext parent);

        default void animateSelf(final T data, final WidgetRenderContext context) {
        }

        Optional<WidgetEvent> animateEvent(T data, WidgetEvent event);
    }

    public enum WidgetEventPhase {
        PRE_CHILD,
        POST_CHILD
    }
}
