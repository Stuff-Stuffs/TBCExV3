package io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.Widget;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class OneHotContainerWidget<T> implements Widget<T> {
    private final Function<? super T, @Nullable Handle> activeScreenGetter;
    private final Int2ObjectSortedMap<AbstractContainerWidget.WidgetInfo<T, ?>> entries = new Int2ObjectAVLTreeMap<>();
    private AbstractContainerWidget.WidgetInfo<T, ?> defaultWidget;
    private int nextId = 0;
    private WidgetContext<T> widgetContext;
    private Rectangle bounds;

    public OneHotContainerWidget(final Function<? super T, @Nullable Handle> activeScreenGetter) {
        this.activeScreenGetter = activeScreenGetter;
    }

    public <K> void setDefaultWidget(final Widget<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> contextFactory) {
        defaultWidget = new AbstractContainerWidget.WidgetInfo<>(widget, contextFactory);
        if (widgetContext != null) {
            setupChild(widgetContext, defaultWidget);
        }
    }

    public <K> Handle addChild(final Widget<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> contextFactory) {
        final AbstractContainerWidget.WidgetInfo<T, K> info = new AbstractContainerWidget.WidgetInfo<>(widget, contextFactory);
        final int id = nextId++;
        entries.put(id, info);
        return new Handle(this, id);
    }

    private void remove(final int id) {
        entries.remove(id);
    }

    @Override
    public void setup(final WidgetContext<T> context) {
        widgetContext = context;
        for (final AbstractContainerWidget.WidgetInfo<T, ?> info : entries.values()) {
            setupChild(context, info);
        }
        if (defaultWidget != null) {
            setupChild(context, defaultWidget);
        }
    }

    private static <T, K> void setupChild(final WidgetContext<T> context, final AbstractContainerWidget.WidgetInfo<T, K> widgetInfo) {
        widgetInfo.widget.setup(widgetInfo.contextFactory.apply(context));
    }

    @Override
    public boolean handleEvent(final WidgetEvent event) {
        final Handle handle = activeScreenGetter.apply(widgetContext.getData());
        if (handle == null) {
            if (defaultWidget != null) {
                return defaultWidget.widget.handleEvent(event);
            }
            return false;
        }
        if (handle.removed) {
            throw new IllegalStateException();
        }
        final AbstractContainerWidget.WidgetInfo<T, ?> info = entries.get(handle.id);
        return info.widget.handleEvent(event);
    }

    @Override
    public Rectangle resize(final RectangleRange range) {
        Rectangle b = range.getMinRectangle();
        for (final AbstractContainerWidget.WidgetInfo<T, ?> info : entries.values()) {
            b = b.union(info.widget.resize(range));
        }
        return b;
    }

    @Override
    public void draw(final WidgetRenderContext context) {
        final Handle handle = activeScreenGetter.apply(widgetContext.getData());
        if (handle == null) {
            if (defaultWidget != null) {
                defaultWidget.widget.draw(context);
            }
            return;
        }
        if (handle.removed) {
            throw new IllegalStateException();
        }
        final AbstractContainerWidget.WidgetInfo<T, ?> info = entries.get(handle.id);
        info.widget.draw(context);
    }

    public static final class Handle {
        private final OneHotContainerWidget<?> parent;
        private final int id;
        private boolean removed = false;

        private Handle(final OneHotContainerWidget<?> parent, final int id) {
            this.parent = parent;
            this.id = id;
        }

        public boolean isRemoved() {
            return removed;
        }

        public void remove() {
            if (!isRemoved()) {
                parent.remove(id);
                removed = true;
            }
        }
    }
}
