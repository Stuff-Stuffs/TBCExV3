package io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.Widget;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

public class GridContainerWidget<T> extends AbstractContainerWidget<T> {
    private final GridContainerSizer<? super T> gridSizer;
    private final DoubleList widths;
    private final DoubleList heights;
    private final Map<Key, WidgetHolder<T, ?>> entries = new Object2ReferenceOpenHashMap<>();


    public GridContainerWidget(final GridContainerSizer<? super T> gridSizer) {
        this.gridSizer = gridSizer;
        widths = new DoubleArrayList();
        heights = new DoubleArrayList();
    }

    public <K> void add(final Widget<K> widget, final Function<WidgetContext<T>, WidgetContext<K>> converter, final int x, final int y) {
        final WidgetInfo<T, K> info = new WidgetInfo<>(widget, converter);
        if (entries.put(new Key(x, y), new WidgetHolder<>(info, false)) != null) {
            throw new IllegalStateException();
        }
        final WidgetContext<T> context = getWidgetContext();
        if (context != null) {
            setup(context, info);
        }
    }

    @Override
    public Rectangle resize(final RectangleRange range) {
        double remainingWidth = range.getMaxRectangle().width();
        double remainingHeight = range.getMaxRectangle().height();
        final T data = getWidgetContext().getData();
        final int xCellCount = gridSizer.xCellCount(data);
        final int yCellCount = gridSizer.yCellCount(data);
        widths.clear();
        heights.clear();
        for (int i = 0; i < xCellCount; i++) {
            final double width = gridSizer.xSize(data, i, remainingWidth, range.getMaxRectangle().width());
            if (width > remainingWidth) {
                throw new IllegalStateException();
            }
            remainingWidth -= width;
            widths.add(width);
        }
        for (int i = 0; i < yCellCount; i++) {
            final double height = gridSizer.ySize(data, i, remainingHeight, range.getMaxRectangle().height());
            if (height > remainingHeight) {
                throw new IllegalStateException();
            }
            remainingHeight -= height;
            heights.add(height);
        }
        double sx = 0;
        for (int i = 0; i < xCellCount; i++) {
            final double width = widths.getDouble(i);
            sx = sx + width;
            double sy = 0;
            for (int j = 0; j < yCellCount; j++) {
                final double height = heights.getDouble(j);
                sy = sy + height;
                final Key key = new Key(i, j);
                final WidgetHolder<T, ?> entry = entries.get(key);
                if (entry != null) {
                    final Point2d lower = range.getMaxRectangle().lower().sum(new Point2d(sx - width, sy - height));
                    final Point2d upper = lower.sum(new Point2d(width, height));
                    final RectangleRange subRange = new RectangleRange(new Rectangle(lower, upper));
                    final Rectangle resize = entry.info().widget.resize(subRange);
                    entry.active = resize.width() > 0 && resize.height() > 0;
                }
            }
        }
        return new Rectangle(range.getMaxRectangle().lower(), range.getMaxRectangle().upper().sum(new Point2d(-remainingWidth, -remainingHeight)));
    }

    private static <T, K> void setup(final WidgetContext<T> context, final WidgetInfo<T, K> entry) {
        entry.widget.setup(entry.contextFactory.apply(context));
    }

    @Override
    protected Iterator<? extends WidgetInfo<T, ?>> getChildren() {
        return entries.values().stream().map(holder -> holder.info).iterator();
    }

    @Override
    protected Iterator<? extends Widget<?>> getChildrenByDrawDepth() {
        return entries.values().stream().filter(WidgetHolder::active).map(holder -> holder.info.widget).iterator();
    }

    @Override
    protected void drawSelf(final WidgetRenderContext context) {

    }

    public interface GridContainerSizer<T> {
        double xSize(T data, int index, double max, double total);

        double ySize(T data, int index, double max, double total);

        int xCellCount(T data);

        int yCellCount(T data);
    }

    private record Key(int x, int y) {
    }

    private static final class WidgetHolder<T, K> {
        private final WidgetInfo<T, K> info;
        private boolean active;

        private WidgetHolder(final WidgetInfo<T, K> info, final boolean active) {
            this.info = info;
            this.active = active;
        }

        public WidgetInfo<T, K> info() {
            return info;
        }

        public boolean active() {
            return active;
        }

        @Override
        public String toString() {
            return "WidgetHolder[" +
                    "info=" + info + ", " +
                    "active=" + active + ']';
        }
    }
}
