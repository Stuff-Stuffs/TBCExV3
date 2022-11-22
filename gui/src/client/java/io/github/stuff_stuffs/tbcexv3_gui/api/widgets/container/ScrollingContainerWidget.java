package io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3_gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.*;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.WidgetRenderUtils;
import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.WidgetUtils;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class ScrollingContainerWidget<T> extends AbstractSingleChildWidget<T> {
    private final Axis axis;
    private final boolean upperScrollbar;
    private final Sizer<? super T> sizer;
    private final ScrollbarInfo<? super T> scrollbarInfo;
    private final StateUpdater<T> stateUpdater;
    private final ScrollbarRenderer<? super T> scrollbarRenderer;
    private Rectangle bounds;
    private Rectangle withoutScroll;
    private Rectangle innerBounds;

    public ScrollingContainerWidget(final Axis axis, final boolean upperScrollbar, final Sizer<? super T> sizer, final ScrollbarInfo<? super T> scrollbarInfo, final StateUpdater<T> stateUpdater, final ScrollbarRenderer<? super T> scrollbarRenderer) {
        this.axis = axis;
        this.upperScrollbar = upperScrollbar;
        this.sizer = sizer;
        this.scrollbarInfo = scrollbarInfo;
        this.stateUpdater = stateUpdater;
        this.scrollbarRenderer = scrollbarRenderer;
    }

    @Override
    public boolean handleEvent(final WidgetEvent event) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        final double scrollAmount = scrollbarInfo.getScrollAmount(widgetContext.getData(), bounds, innerBounds);
        final Optional<WidgetEvent> transform = event.transform(p -> {
            if (withoutScroll.contains(p)) {
                final Point2d offset = new Point2d(0, -scrollAmount).convert(axis);
                return p.combine(offset, Double::sum);
            }
            return null;
        }, Function.identity());
        if (transform.isPresent()) {
            final WidgetInfo<T, ?> child = getChild();
            if (child == null) {
                throw new NullPointerException();
            }
            final boolean b = child.widget.handleEvent(transform.get());
            return b || stateUpdater.event(event, widgetContext.getData());
        }
        return stateUpdater.event(event, widgetContext.getData());
    }

    @Override
    public void draw(final WidgetRenderContext context) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        drawSelf(context);
        final double scrollAmount = scrollbarInfo.getScrollAmount(widgetContext.getData(), bounds, innerBounds);
        final double criticalLength = axis.choose(bounds.width(), bounds.height());
        final double criticalInnerLength = axis.choose(innerBounds.width(), innerBounds.height());
        final double scrollNorm = (scrollAmount / criticalInnerLength) * (criticalInnerLength - criticalLength);
        context.pushTransform(new WidgetRenderContext.ScissorTransform(withoutScroll));
        context.pushTransform(new WidgetRenderContext.Translate(axis.choose(scrollNorm, 0), axis.choose(0, scrollNorm)));
        getChildrenByDrawDepth().forEachRemaining(widget -> widget.draw(context));
        context.popTransform();
        context.popTransform();
    }

    @Override
    protected void drawSelf(final WidgetRenderContext context) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        scrollbarRenderer.render(widgetContext.getData(), context, bounds, innerBounds, axis);
    }

    @Override
    public Rectangle resize(final RectangleRange range) {
        final WidgetContext<T> widgetContext = getWidgetContext();
        if (widgetContext == null) {
            throw new NullPointerException();
        }
        bounds = sizer.calculateSize(widgetContext.getData(), range);
        stateUpdater.updateBounds(bounds, widgetContext.getData());

        final double scrollbarSize = scrollbarInfo.calculateScrollbarSize(widgetContext.getData(), bounds);
        if (axis.choose(bounds.height(), bounds.width()) < scrollbarSize) {
            throw new IllegalStateException();
        }
        if (upperScrollbar) {
            final Point2d offset = new Point2d(0, -scrollbarSize).convert(axis);
            withoutScroll = new Rectangle(bounds.lower(), bounds.upper().combine(offset, Double::sum));
        } else {
            final Point2d offset = new Point2d(0, scrollbarSize).convert(axis);
            withoutScroll = new Rectangle(bounds.lower().combine(offset, Double::sum), bounds.upper());
        }
        final RectangleRange subRange = RectangleRange.max(withoutScroll).expandExtents(axis.choose(1_000_000, 0), axis.choose(0, 1_000_000));

        final WidgetInfo<T, ?> child = getChild();
        if (child == null) {
            throw new NullPointerException();
        }
        innerBounds = child.widget.resize(subRange);
        if (widgetContext.getData() instanceof InnerBoundsHolder innerBoundsHolder) {
            innerBoundsHolder.setInnerBounds(innerBounds);
        }
        return bounds;
    }

    public interface InnerBoundsHolder extends WidgetUtils.MutableBoundsHolder {
        Rectangle innerBounds();

        void setInnerBounds(Rectangle rectangle);
    }

    public interface ScrollbarInfo<T> {
        double calculateScrollbarSize(T data, Rectangle bounds);

        double getScrollAmount(T data, Rectangle bounds, Rectangle innerBounds);

        double getScrollbarLength(T data, Rectangle bounds, Rectangle innerBounds);
    }

    public interface ScrollbarRenderer<T> {
        void render(T data, WidgetRenderContext renderContext, Rectangle bounds, Rectangle innerBounds, Axis axis);
    }

    public interface ScrollbarState extends InnerBoundsHolder {
        double getScrollAmount();

        void setScrollAmount(double scrollAmount);

        static <T extends ScrollbarState> StateUpdater<T> stateUpdater(final Axis axis) {
            final WidgetUtils.StateUpdaterBuilder<T> builder = WidgetUtils.builder();
            builder.addTransforming(data -> data, WidgetUtils.MutableBoundsHolder.stateUpdater());
            builder.add((event, data) -> {
                if (event instanceof WidgetEvent.MouseScrollEvent scrollEvent) {
                    if (data.bounds().contains(scrollEvent.point())) {
                        data.setScrollAmount(Math.max(0, Math.min(data.getScrollAmount() + scrollEvent.amount(), axis.choose(data.innerBounds().width(), data.innerBounds().height()))));
                        return true;
                    }
                }
                if (event instanceof WidgetEvent.MousePressEvent pressEvent) {
                    if (pressEvent.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                        return false;
                    }
                    if (data.bounds().contains(pressEvent.point()) && !data.innerBounds().contains(pressEvent.point())) {
                        final double progress = (1 - (axis.choose(pressEvent.point()) - axis.choose(data.bounds().lower()))) / axis.choose(data.bounds().width(), data.bounds().height());
                        data.setScrollAmount(Math.max(0, Math.min(progress * axis.choose(data.innerBounds().width(), data.innerBounds().height()), axis.choose(data.innerBounds().width(), data.innerBounds().height()))));
                        return true;
                    }
                }
                return false;
            });
            return builder.build();
        }

        static <K> WidgetContext<ScrollbarState> standalone(final WidgetContext<K> parentContext) {
            return WidgetContext.standalone(parentContext, new ScrollbarState() {
                private double scrollAmount = 0;
                private Rectangle bounds;
                private Rectangle innerBounds;

                @Override
                public Rectangle innerBounds() {
                    return innerBounds;
                }

                @Override
                public void setInnerBounds(final Rectangle rectangle) {
                    innerBounds = rectangle;
                }

                @Override
                public void setBounds(final Rectangle bounds) {
                    this.bounds = bounds;
                }

                @Override
                public Rectangle bounds() {
                    return bounds;
                }

                @Override
                public double getScrollAmount() {
                    return scrollAmount;
                }

                @Override
                public void setScrollAmount(final double scrollAmount) {
                    this.scrollAmount = scrollAmount;
                }
            });
        }
    }

    public static <T> ScrollbarRenderer<T> basicScrollbarRenderer(final boolean scrollBarUpper, final ScrollbarInfo<? super T> scrollbarInfo, final ToIntFunction<? super T> colorFunction) {
        return (data, renderContext, bounds, innerBounds, axis) -> {
            final double scrollbarSize = scrollbarInfo.calculateScrollbarSize(data, bounds);
            final double progress = scrollbarInfo.getScrollAmount(data, bounds, innerBounds);
            final double a2Size = axis.choose(bounds.width(), bounds.height());
            final double a2InnerSize = axis.choose(innerBounds.width(), innerBounds.height());
            final double scrollBarLength = scrollbarInfo.getScrollbarLength(data, bounds, innerBounds);
            final double startAxis = axis.choose(bounds.lower().x(), bounds.lower().y()) + (progress / a2InnerSize) * (a2Size - scrollBarLength);
            final Rectangle rectangle;
            if (scrollBarUpper) {
                final Point2d start = new Point2d(startAxis, axis.choose(bounds.upper().y(), bounds.upper().x()) - scrollbarSize).convert(axis);
                final Point2d end = new Point2d(scrollbarSize, scrollBarLength).combine(start, Double::sum);
                rectangle = new Rectangle(start, end);
            } else {
                final Point2d start = new Point2d(startAxis, axis.choose(bounds.lower().y(), bounds.lower().x())).convert(axis);
                final Point2d end = new Point2d(scrollbarSize, scrollBarLength).combine(start, Double::sum);
                rectangle = new Rectangle(start, end);
            }
            WidgetRenderUtils.drawRectangle(renderContext.emitter(), rectangle, colorFunction.applyAsInt(data));
        };
    }
}
