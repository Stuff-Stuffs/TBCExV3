package io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.Widget;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
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
        if (getWidgetContext() != null) {
            setupChild(getWidgetContext(), child);
            getWidgetContext().forceResize();
        }
    }

    @Override
    public void setup(final WidgetContext<T> context) {
        super.setup(context);
        if (child != null) {
            setFocus(child.widget);
        }
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
    public Rectangle resize(final RectangleRange range) {
        if (child == null) {
            return range.getMinRectangle();
        }
        return child.widget.resize(range);
    }
}
