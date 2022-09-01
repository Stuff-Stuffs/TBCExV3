package io.github.stuff_stuffs.tbcexv3gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.Widget;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

public abstract class AbstractContainerWidget<T> implements Widget<T> {
    private WidgetContext<T> widgetContext;
    private boolean focused = false;
    private Widget<?> focus = null;

    protected abstract Iterator<? extends WidgetInfo<T, ?>> getChildren();

    protected abstract Iterator<? extends Widget<?>> getChildrenByDrawDepth();

    protected abstract void drawSelf(WidgetRenderContext context);

    protected void setFocus(final Widget<?> newFocus) {
        if (focus != null) {
            focus.handleEvent(new WidgetEvent.LostFocusEvent());
        }
        focus = newFocus;
        focus.handleEvent(new WidgetEvent.GainedFocusEvent());
    }

    protected @Nullable Widget<?> getFocus() {
        return focus;
    }

    protected boolean isFocused() {
        return focused;
    }

    protected @Nullable WidgetContext<T> getWidgetContext() {
        return widgetContext;
    }

    @Override
    public void setup(final WidgetContext<T> context) {
        widgetContext = context;
        getChildren().forEachRemaining(child -> setupChild(context, child));
    }

    @Override
    public boolean handleEvent(final WidgetEvent event) {
        if (event instanceof WidgetEvent.LostFocusEvent) {
            if (focused) {
                if (focus != null) {
                    focus.handleEvent(event);
                }
                focused = false;
                return true;
            }
            return false;
        } else if (event instanceof WidgetEvent.GainedFocusEvent) {
            if (focused) {
                if (focus != null) {
                    focus.handleEvent(event);
                }
                focused = true;
                return true;
            }
            return false;
        } else {
            final Iterator<? extends Widget<?>> iterator = getChildrenByDrawDepth();
            while (iterator.hasNext()) {
                final Widget<?> widget = iterator.next();
                if (widget.handleEvent(event)) {
                    if (!event.alwaysPass()) {
                        return true;
                    }
                    setFocus(widget);
                }
            }
        }
        return false;
    }

    @Override
    public void draw(final WidgetRenderContext context) {
        drawSelf(context);
        getChildrenByDrawDepth().forEachRemaining(widget -> widget.draw(context));
    }

    private static <T, K> void setupChild(final WidgetContext<T> context, final WidgetInfo<T, K> widgetInfo) {
        widgetInfo.widget.setup(widgetInfo.contextFactory.apply(context));
    }

    public static final class WidgetInfo<T, K> {
        public final Widget<K> widget;
        public final Function<WidgetContext<T>, WidgetContext<K>> contextFactory;

        public WidgetInfo(final Widget<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> contextFactory) {
            this.widget = widget;
            this.contextFactory = contextFactory;
        }
    }
}
