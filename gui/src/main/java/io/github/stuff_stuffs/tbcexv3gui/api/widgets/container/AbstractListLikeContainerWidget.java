package io.github.stuff_stuffs.tbcexv3gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.Axis;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.Widget;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.WidgetRenderUtils;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.function.Function;

public abstract class AbstractListLikeContainerWidget<T> extends AbstractContainerWidget<T> {
    private final WidgetRenderUtils.Renderer<? super T> renderer;
    private final ListLikeEntrySizer<? super T> widthSizer;
    private final SortedSet<WidgetEntry<T, ?>> entries = new ObjectAVLTreeSet<>(WidgetEntry.COMPARATOR);
    private final Justification justification;
    private final Axis axis;
    private final boolean useActualWidth;
    private final boolean resizeToWidgetBounds;
    private Rectangle bounds;

    protected AbstractListLikeContainerWidget(final WidgetRenderUtils.Renderer<? super T> renderer, final ListLikeEntrySizer<? super T> widthSizer, final Justification justification, final Axis axis, final boolean useActualWidth, final boolean resizeToWidgetBounds) {
        this.renderer = renderer;
        this.widthSizer = widthSizer;
        this.justification = justification;
        this.axis = axis;
        this.useActualWidth = useActualWidth;
        this.resizeToWidgetBounds = resizeToWidgetBounds;
    }


    protected <K> @Nullable WidgetInfo<T, ?> setChild(final Widget<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> contextFactory, final int index) {
        final WidgetEntry<T, ?> newEntry = new WidgetEntry<>(new WidgetInfo<>(widget, contextFactory), index);
        final SortedSet<WidgetEntry<T, ?>> tail = entries.tailSet(newEntry);
        WidgetInfo<T, ?> previous = null;
        if (!tail.isEmpty()) {
            final WidgetEntry<T, ?> first = tail.first();
            if (first.index == index) {
                previous = first.widgetInfo;
                entries.remove(first);
            }
        }
        if (widget != null) {
            entries.add(newEntry);
            if (getWidgetContext() != null) {
                if (getWidgetContext() != null) {
                    setupChild(getWidgetContext(), newEntry.widgetInfo);
                    getWidgetContext().forceResize();
                }
            }
            return previous;
        } else {
            return null;
        }
    }

    @Override
    protected Iterator<? extends WidgetInfo<T, ?>> getChildren() {
        return entries.stream().map(entry -> entry.widgetInfo).iterator();
    }

    @Override
    protected Iterator<? extends Widget<?>> getChildrenByDrawDepth() {
        return entries.stream().map(entry -> entry.widgetInfo.widget).iterator();
    }

    @Override
    protected void drawSelf(final WidgetRenderContext context) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        renderer.render(widgetContext.getData(), context, bounds);
    }

    @Override
    public Rectangle resize(final RectangleRange range) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        final double startA1 = widthSizer.getA1Start(widgetContext.getData(), range, axis);
        final double endA1 = widthSizer.getA1End(widgetContext.getData(), range, axis);
        final double startA2 = widthSizer.getA2Start(widgetContext.getData(), range, axis);
        final double endA2 = widthSizer.getA2End(widgetContext.getData(), range, axis);
        final double maxA1 = endA1 - startA1;
        if (maxA1 > axis.choose(range.getMaxExtents().x(), range.getMaxExtents().y())) {
            throw new IllegalStateException();
        }
        double remainingA1 = maxA1;
        double axisPos = switch (justification) {
            case LOWER -> startA1;
            case UPPER -> endA1;
        };
        final int maxIndex = entries.isEmpty() ? 0 : entries.last().index;
        final Iterator<WidgetEntry<T, ?>> iterator = entries.iterator();
        double minA2 = axis.choose(range.getOuterLow().y(), range.getOuterLow().x());
        double maxA2 = axis.choose(range.getOuterHigh().y(), range.getOuterHigh().x());
        while (remainingA1 > 0 && iterator.hasNext()) {
            final WidgetEntry<T, ?> widgetEntry = iterator.next();
            final double minEntryWidth = widthSizer.getMaxSize(widgetContext.getData(), axisPos, maxA1, remainingA1, widgetEntry.index, maxIndex, axis);
            final double maxEntryWidth = widthSizer.getMaxSize(widgetContext.getData(), axisPos, maxA1, remainingA1, widgetEntry.index, maxIndex, axis);
            if (maxEntryWidth < minEntryWidth || maxA1 < maxEntryWidth) {
                throw new IllegalStateException();
            }
            final RectangleRange bounds = getBounds(axisPos, minEntryWidth, maxEntryWidth, startA2, endA2);
            final Rectangle chosen = widgetEntry.widgetInfo.widget.resize(bounds);
            final double size;
            if (useActualWidth) {
                size = axis.choose(chosen.width(), chosen.height());
            } else {
                size = maxA1;
            }
            remainingA1 = remainingA1 - size;
            switch (justification) {
                case LOWER -> axisPos = axisPos + size;
                case UPPER -> axisPos = axisPos - size;
            }
            minA2 = Math.min(minA2, axis.choose(chosen.lower().y(), chosen.lower().x()));
            maxA2 = Math.max(maxA2, axis.choose(chosen.upper().y(), chosen.upper().x()));
        }
        while (iterator.hasNext()) {
            final WidgetEntry<T, ?> widgetEntry = iterator.next();
            widgetEntry.widgetInfo.widget.resize(getBounds(axisPos, 0, 0, minA2, maxA2));
        }
        final Rectangle rect;
        if (resizeToWidgetBounds) {
            rect = getSelfBounds(startA1, maxA1, remainingA1, minA2, maxA2);
        } else {
            rect = range.getMaxRectangle();
        }
        return bounds = rect;
    }

    private Rectangle getSelfBounds(final double axisPos, final double maxA1, final double remainingA1, final double minA2, final double maxA2) {
        if (justification == Justification.LOWER) {
            final double maxPosA1 = axisPos + maxA1 - remainingA1;
            return new Rectangle(new Point2d(axisPos, minA2).convert(axis), new Point2d(maxPosA1, maxA2).convert(axis));
        } else {
            final double minPosA1 = axisPos - maxA1 + remainingA1;
            return new Rectangle(new Point2d(minPosA1, minA2).convert(axis), new Point2d(axisPos, maxA2).convert(axis));
        }
    }

    private RectangleRange getBounds(final double axisPos, final double minEntryA1, final double maxEntryA1, final double startA2, final double endA2) {
        if (justification == Justification.LOWER) {
            final Point2d lower = new Point2d(axisPos, startA2).convert(axis);
            final Point2d minUpper = new Point2d(axisPos + minEntryA1, endA2).convert(axis);
            final Point2d maxUpper = new Point2d(axisPos + maxEntryA1, endA2).convert(axis);
            return new RectangleRange(lower, lower, maxUpper, minUpper);
        } else {
            final Point2d minLower = new Point2d(axisPos - minEntryA1, startA2).convert(axis);
            final Point2d maxLower = new Point2d(axisPos - maxEntryA1, startA2).convert(axis);
            final Point2d upper = new Point2d(axisPos, endA2).convert(axis);
            return new RectangleRange(minLower, maxLower, upper, upper);
        }
    }

    private static final class WidgetEntry<T, K> {
        public static final Comparator<WidgetEntry<?, ?>> COMPARATOR = Comparator.comparingInt(entry -> entry.index);
        public final AbstractContainerWidget.WidgetInfo<T, K> widgetInfo;
        public final int index;

        public WidgetEntry(final AbstractContainerWidget.WidgetInfo<T, K> widgetInfo, final int index) {
            this.widgetInfo = widgetInfo;
            this.index = index;
        }
    }

    public interface ListLikeEntrySizer<T> {
        double getMinSize(T data, double startA1, double maxA1, double remainingA1, int rowIndex, int maxIndex, Axis axis);

        default double getMaxSize(final T data, final double startA1, final double maxA1, final double remainingA1, final int rowIndex, final int maxIndex, final Axis axis) {
            return getMinSize(data, startA1, maxA1, remainingA1, rowIndex, maxIndex, axis);
        }

        default double getA1Start(final T data, final RectangleRange range, final Axis axis) {
            return axis.choose(range.getOuterLow().x(), range.getOuterLow().y());
        }

        default double getA1End(final T data, final RectangleRange range, final Axis axis) {
            return axis.choose(range.getOuterHigh().x(), range.getOuterHigh().y());
        }

        default double getA2Start(final T data, final RectangleRange range, final Axis axis) {
            return axis.choose(range.getOuterLow().y(), range.getOuterLow().x());
        }

        default double getA2End(final T data, final RectangleRange range, final Axis axis) {
            return axis.choose(range.getOuterHigh().y(), range.getOuterHigh().x());
        }
    }

    public enum Justification {
        UPPER,
        LOWER
    }
}
