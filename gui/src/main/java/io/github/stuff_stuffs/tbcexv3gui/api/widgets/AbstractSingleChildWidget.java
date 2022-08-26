package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractSingleChildWidget<T> extends AbstractContainerWidget<T> {
    private WidgetInfo<T, ?> child = null;

    public <K> void setChild(final Widget<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> converter) {
        if (child != null) {
            throw new RuntimeException();
        }
        child = new WidgetInfo<>(widget, converter);
    }

    protected @Nullable WidgetInfo<T, ?> getChild() {
        return child;
    }

    @Override
    protected Iterator<? extends WidgetInfo<T, ?>> getChildren() {
        if (child == null) {
            return Collections.emptyIterator();
        }
        return List.of(child).iterator();
    }

    @Override
    protected Iterator<? extends Widget<?>> getChildrenByDrawDepth() {
        if (child == null) {
            return Collections.emptyIterator();
        }
        return List.of(child.widget).iterator();
    }

    @Override
    public boolean handleEvent(final WidgetEvent event) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        if (!super.handleEvent(event)) {
            return event.shouldPassToChildren() && child.widget.handleEvent(event);
        } else {
            return true;
        }
    }

    @Override
    public Rectangle resize(final Rectangle min, final Rectangle max) {
        if (child == null) {
            return min;
        }
        return child.widget.resize(min, max);
    }
}
