package io.github.stuff_stuffs.tbcexv3_gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Trapezoid;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.StateUpdater;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.function.Function;

public class SelectionWheelWidget<T> implements Widget<T> {
    private final RadiusSizer<? super T> radiusSizer;
    private final StateUpdater<? super T> stateUpdater;
    private final SortedSet<Entry<T, ?>> entries;
    private WidgetContext<T> widgetContext;
    private int nextId = 0;

    public SelectionWheelWidget(final RadiusSizer<? super T> radiusSizer, final StateUpdater<? super T> stateUpdater) {
        this.radiusSizer = radiusSizer;
        this.stateUpdater = stateUpdater;
        entries = new ObjectAVLTreeSet<>(Entry.COMPARATOR);
    }

    @Override
    public void setup(final WidgetContext<T> context) {
        widgetContext = context;
        for (final Entry<T, ?> entry : entries) {
            setupChild(context, entry);
        }
    }

    public <K> Handle add(final Section<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> contextFactory) {
        final Handle handle = new Handle(this, nextId++);
        final Entry<T, K> entry = new Entry<>(widget, contextFactory, handle.id);
        entries.add(entry);
        if (widgetContext != null) {
            setupChild(widgetContext, entry);
            widgetContext.forceResize();
        }
        return handle;
    }

    private static <T, K> void setupChild(final WidgetContext<T> context, final Entry<T, K> widgetInfo) {
        widgetInfo.section.setup(widgetInfo.contextFactory.apply(context));
    }

    @Override
    public boolean handleEvent(final WidgetEvent event) {
        final WidgetContext<T> context = widgetContext;
        if (context == null) {
            throw new NullPointerException();
        }
        for (final Entry<T, ?> entry : entries) {
            if (entry.section.handleEvent(event)) {
                return true;
            }
        }
        return stateUpdater.event(event, context.getData());
    }

    @Override
    public Rectangle resize(final RectangleRange range) {
        final WidgetContext<T> context = widgetContext;
        if (context == null) {
            throw new NullPointerException();
        }
        final Point2d center = radiusSizer.getCenter(context.getData(), range);
        final double innerRadius = radiusSizer.getInnerRadius(context.getData(), range);
        final double outerRadius = radiusSizer.getOuterRadius(context.getData(), range);
        final double outerHoverRadius = radiusSizer.getOuterHoverRadius(context.getData(), range);
        if (innerRadius > outerRadius || innerRadius > outerHoverRadius) {
            throw new IllegalStateException();
        }
        final Rectangle rect = new Rectangle(center.combine(new Point2d(-outerRadius, -outerRadius), Double::sum), center.combine(new Point2d(outerRadius, outerRadius), Double::sum));
        if (!range.getMaxRectangle().contains(rect)) {
            throw new IllegalStateException();
        }
        final int i = 0;
        final int size = entries.size();
        for (final Entry<T, ?> entry : entries) {
            final Trapezoid trapezoid = new Trapezoid(innerRadius, outerRadius, i / (double) size * Math.PI * 2, (i + 1) / (double) size * Math.PI * 2, center);
            final Trapezoid hoverTrapezoid = new Trapezoid(innerRadius, outerHoverRadius, i / (double) size * Math.PI * 2, (i + 1) / (double) size * Math.PI * 2, center);
            entry.section.setTrapezoid(trapezoid, hoverTrapezoid);
        }
        return rect;
    }

    @Override
    public void draw(final WidgetRenderContext context) {
        for (final Entry<T, ?> entry : entries) {
            entry.section.draw(context);
        }
    }

    private void remove(final int id) {
        final Entry<T, ?> entry = new Entry<>(null, null, id);
        final SortedSet<Entry<T, ?>> tailSet = entries.tailSet(entry);
        if (tailSet.first().index == id) {
            tailSet.remove(tailSet.first());
        }
    }

    public interface RadiusSizer<T> {
        double getInnerRadius(T data, RectangleRange range);

        double getOuterRadius(T data, RectangleRange range);

        double getOuterHoverRadius(T data, RectangleRange range);

        Point2d getCenter(T data, RectangleRange range);
    }

    public interface Section<T> {
        void setup(WidgetContext<T> context);

        void setTrapezoid(Trapezoid bounds, Trapezoid hoverBounds);

        void draw(WidgetRenderContext context);

        boolean handleEvent(WidgetEvent event);
    }

    public static class BasicSection<T> implements Section<T> {
        private final SectionStateUpdater<? super T> stateUpdater;
        private final SectionRenderer<? super T> renderer;
        private WidgetContext<T> widgetContext;
        private Trapezoid bounds;
        private Trapezoid hoverBounds;

        public BasicSection(final SectionStateUpdater<? super T> stateUpdater, final SectionRenderer<? super T> renderer) {
            this.stateUpdater = stateUpdater;
            this.renderer = renderer;
        }

        @Override
        public void setup(final WidgetContext<T> context) {
            widgetContext = context;
        }

        @Override
        public void setTrapezoid(final Trapezoid bounds, final Trapezoid hoverBounds) {
            this.bounds = bounds;
            this.hoverBounds = hoverBounds;
            stateUpdater.resize(bounds, hoverBounds);
        }

        @Override
        public void draw(final WidgetRenderContext context) {
            renderer.render(widgetContext.getData(), context, bounds, hoverBounds);
        }

        @Override
        public boolean handleEvent(final WidgetEvent event) {
            return stateUpdater.handleEvent(widgetContext.getData(), bounds, hoverBounds);
        }
    }

    public interface SectionStateUpdater<T> {
        boolean handleEvent(T data, Trapezoid bounds, Trapezoid hoverBounds);

        default void resize(final Trapezoid bounds, final Trapezoid hoverBounds) {
        }
    }

    public interface SectionRenderer<T> {
        void render(T data, WidgetRenderContext renderContext, Trapezoid bounds, Trapezoid hoverBounds);

        static <T> SectionRenderer<T> empty() {
            return (data, renderContext, bounds, hoverBounds) -> {
            };
        }
    }

    private static final class Entry<T, K> {
        private static final Comparator<Entry<?, ?>> COMPARATOR = Comparator.comparingInt(entry -> entry.index);
        private final Section<K> section;
        private final Function<WidgetContext<T>, WidgetContext<K>> contextFactory;
        private final int index;

        private Entry(final Section<K> section, final Function<WidgetContext<T>, WidgetContext<K>> contextFactory, final int index) {
            this.section = section;
            this.contextFactory = contextFactory;
            this.index = index;
        }
    }

    public static final class Handle {
        private final SelectionWheelWidget<?> parent;
        private final int id;
        private boolean removed = false;

        private Handle(final SelectionWheelWidget<?> parent, final int id) {
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
