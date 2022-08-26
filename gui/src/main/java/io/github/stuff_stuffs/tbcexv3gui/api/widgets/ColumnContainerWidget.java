package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3gui.api.Point2d;
import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.function.Function;

public class ColumnContainerWidget<T> extends AbstractContainerWidget<T> {
    private final WidgetRenderUtils.Renderer<? super T> renderer;
    private final ColumnWidthSizer<? super T> widthSizer;
    private final SortedSet<WidgetEntry<T, ?>> entries;
    private final Justification justification;
    private final boolean useActualWidth;
    private final boolean resizeToWidgetBounds;
    private Rectangle bounds;

    public ColumnContainerWidget(final WidgetRenderUtils.Renderer<? super T> renderer, final ColumnWidthSizer<? super T> widthSizer, final Justification justification, final boolean useActualWidth, final boolean resizeToWidgetBounds) {
        this.renderer = renderer;
        this.widthSizer = widthSizer;
        this.justification = justification;
        this.useActualWidth = useActualWidth;
        this.resizeToWidgetBounds = resizeToWidgetBounds;
        entries = new ObjectAVLTreeSet<>(WidgetEntry.COMPARATOR);
    }

    public <K> @Nullable WidgetInfo<T, ?> setChild(final Widget<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> contextFactory, final int column) {
        final WidgetEntry<T, ?> newEntry = new WidgetEntry<>(new WidgetInfo<>(widget, contextFactory), column);
        final SortedSet<WidgetEntry<T, ?>> tail = entries.tailSet(newEntry);
        WidgetInfo<T, ?> previous = null;
        if (!tail.isEmpty()) {
            final WidgetEntry<T, ?> first = tail.first();
            if (first.columnIndex == column) {
                previous = first.widgetInfo;
                entries.remove(first);
            }
        }
        entries.add(newEntry);
        return previous;
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
    public Rectangle resize(final Rectangle min, final Rectangle max) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        final double xStart = widthSizer.getXStart(widgetContext.getData(), min, max);
        final double xEnd = widthSizer.getXEnd(widgetContext.getData(), min, max);
        final double yStart = widthSizer.getYStart(widgetContext.getData(), min, max);
        final double yEnd = widthSizer.getYEnd(widgetContext.getData(), min, max);
        final double maxWidth = xEnd - xStart;
        if (maxWidth + max.lower().x() > max.upper().x()) {
            throw new IllegalStateException();
        }
        double widthRemaining = maxWidth;
        double xPos = switch (justification) {
            case LEFT -> xStart;
            case RIGHT -> xEnd;
        };
        final Iterator<WidgetEntry<T, ?>> iterator = entries.iterator();
        double minY = min.lower().y();
        double maxY = min.upper().y();
        while (widthRemaining > 0 && iterator.hasNext()) {
            final WidgetEntry<T, ?> widgetEntry = iterator.next();
            final double minEntryWidth = widthSizer.getMinColumnWidth(widgetContext.getData(), xPos, maxWidth, widthRemaining, widgetEntry.columnIndex);
            final double maxEntryWidth = widthSizer.getMaxColumnWidth(widgetContext.getData(), xPos, maxWidth, widthRemaining, widgetEntry.columnIndex);
            if (maxEntryWidth < minEntryWidth || maxWidth < maxEntryWidth) {
                throw new IllegalStateException();
            }
            final Pair<Rectangle, Rectangle> bounds = getBounds(xPos, minEntryWidth, maxEntryWidth, yStart, yEnd);
            final Rectangle chosen = widgetEntry.widgetInfo.widget.resize(bounds.getFirst(), bounds.getSecond());
            final double width;
            if (useActualWidth) {
                width = chosen.upper().x() - chosen.lower().x();
            } else {
                width = maxWidth;
            }
            widthRemaining = widthRemaining - width;
            switch (justification) {
                case LEFT -> xPos = xPos + width;
                case RIGHT -> xPos = xPos - width;
            }
            minY = Math.min(minY, chosen.lower().y());
            maxY = Math.max(maxY, chosen.upper().y());
        }
        final Rectangle emptyBounds = new Rectangle(max.lower(), max.lower());
        while (iterator.hasNext()) {
            final WidgetEntry<T, ?> widgetEntry = iterator.next();
            widgetEntry.offset = max.lower().x() + maxWidth;
            widgetEntry.width = 0;
            widgetEntry.widgetInfo.widget.resize(emptyBounds, emptyBounds);
        }
        if (resizeToWidgetBounds) {
            if (justification == Justification.LEFT) {
                return bounds = new Rectangle(new Point2d(max.lower().x(), minY), new Point2d(xPos, maxY));
            } else {
                return bounds = new Rectangle(new Point2d(xPos, minY), new Point2d(max.upper().x(), maxY));
            }
        }
        return bounds = max;
    }

    private Pair<Rectangle, Rectangle> getBounds(final double xPos, final double minEntryWidth, final double maxEntryWidth, final double yStart, final double yEnd) {
        if (justification == Justification.LEFT) {
            final Point2d lower = new Point2d(xPos, yStart);
            final Point2d minUpper = new Point2d(xPos + minEntryWidth, yEnd);
            final Point2d maxUpper = new Point2d(xPos + maxEntryWidth, yEnd);
            return Pair.of(new Rectangle(lower, minUpper), new Rectangle(lower, maxUpper));
        } else {
            final Point2d minLower = new Point2d(xPos - minEntryWidth, yStart);
            final Point2d maxLower = new Point2d(xPos - maxEntryWidth, yStart);
            final Point2d upper = new Point2d(xPos, yEnd);
            return Pair.of(new Rectangle(minLower, upper), new Rectangle(maxLower, upper));
        }
    }


    private static final class WidgetEntry<T, K> {
        public static final Comparator<WidgetEntry<?, ?>> COMPARATOR = Comparator.comparingInt(entry -> entry.columnIndex);
        public final WidgetInfo<T, K> widgetInfo;
        public final int columnIndex;
        public double offset = 0;
        public double width = 0;

        public WidgetEntry(final WidgetInfo<T, K> widgetInfo, final int columnIndex) {
            this.widgetInfo = widgetInfo;
            this.columnIndex = columnIndex;
        }
    }

    public interface ColumnWidthSizer<T> {
        double getMinColumnWidth(T data, double startX, double maxWidth, double widthRemaining, int columnIndex);

        default double getMaxColumnWidth(final T data, final double startX, final double maxWidth, final double widthRemaining, final int columnIndex) {
            return getMinColumnWidth(data, startX, maxWidth, widthRemaining, columnIndex);
        }

        default double getXStart(final T data, final Rectangle min, final Rectangle max) {
            return max.lower().x();
        }

        default double getXEnd(final T data, final Rectangle min, final Rectangle max) {
            return max.upper().x();
        }

        default double getYStart(final T data, final Rectangle min, final Rectangle max) {
            return max.lower().y();
        }

        default double getYEnd(final T data, final Rectangle min, final Rectangle max) {
            return max.upper().y();
        }
    }

    public enum Justification {
        LEFT,
        RIGHT
    }
}
