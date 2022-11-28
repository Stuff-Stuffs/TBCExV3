package io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;

import java.util.Optional;

public class ModifiableWidget<T> extends AbstractSingleChildWidget<T> {
    private final Animation<? super T> animation;
    private final StateUpdater<? super T> stateUpdater;
    private final WidgetEventPhase eventPhase;

    public ModifiableWidget(final Animation<? super T> animation, final StateUpdater<? super T> stateUpdater, final WidgetEventPhase eventPhase) {
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
    public Rectangle resize(final RectangleRange range) {
        final Rectangle rectangle = super.resize(range);
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

        static <T> Animation<T> none() {
            return new Animation<>() {
                @Override
                public Optional<WidgetRenderContext> animate(final T data, final WidgetRenderContext parent) {
                    return Optional.of(parent);
                }

                @Override
                public Optional<WidgetEvent> animateEvent(final T data, final WidgetEvent event) {
                    return Optional.of(event);
                }
            };
        }
    }

    public enum WidgetEventPhase {
        PRE_CHILD,
        POST_CHILD
    }
}
