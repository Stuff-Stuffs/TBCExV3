package io.github.stuff_stuffs.tbcexv3gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3gui.api.widget.Axis;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.Widget;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.WidgetRenderUtils;

import java.util.function.Function;

public class ExpandableListContainerWidget<T> extends AbstractListLikeContainerWidget<T> implements CollectionLikeWidget<T, ExpandableListContainerWidget.Handle> {
    private int nextId = 0;

    public ExpandableListContainerWidget(final WidgetRenderUtils.Renderer<? super T> renderer, final ListLikeEntrySizer<? super T> widthSizer, final Justification justification, final Axis axis, final boolean useActualWidth) {
        super(renderer, widthSizer, justification, axis, useActualWidth, true);
    }

    @Override
    public <K> Handle add(final Widget<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> contextFactory) {
        final Handle handle = new Handle(this, nextId++);
        setChild(widget, contextFactory, handle.id);
        return handle;
    }

    public static final class Handle {
        private final ExpandableListContainerWidget<?> container;
        private final int id;
        private boolean removed;

        private Handle(final ExpandableListContainerWidget<?> container, final int id) {
            this.container = container;
            this.id = id;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void remove() {
            if (!isRemoved()) {
                container.setChild(null, null, id);
                removed = true;
            }
        }
    }
}
