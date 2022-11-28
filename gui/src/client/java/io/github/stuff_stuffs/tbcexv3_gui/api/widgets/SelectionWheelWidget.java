package io.github.stuff_stuffs.tbcexv3_gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Trapezoid;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.*;
import io.github.stuff_stuffs.tbcexv3_gui.internal.client.TBCExV3GuiClient;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.function.*;

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
        if (innerRadius > outerRadius || outerRadius > outerHoverRadius) {
            throw new IllegalStateException();
        }
        final Rectangle rect = new Rectangle(center.combine(new Point2d(-outerHoverRadius, -outerHoverRadius), Double::sum), center.combine(new Point2d(outerHoverRadius, outerHoverRadius), Double::sum));
        if (!range.getMaxRectangle().contains(rect)) {
            throw new IllegalStateException();
        }
        int i = 0;
        final int size = entries.size();
        for (final Entry<T, ?> entry : entries) {
            final Trapezoid trapezoid = new Trapezoid(innerRadius, outerRadius, i / (double) size * Math.PI * 2, (i + 1) / (double) size * Math.PI * 2, center);
            final Trapezoid hoverTrapezoid = new Trapezoid(innerRadius, outerHoverRadius, i / (double) size * Math.PI * 2, (i + 1) / (double) size * Math.PI * 2, center);
            i++;
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

    @Override
    public void postDraw(final MatrixStack stack, final VertexConsumerProvider vertexConsumers, final Rectangle screenBounds) {
        entries.forEach(entry -> entry.section.postDraw(stack, vertexConsumers, screenBounds));
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

        static <T> RadiusSizer<T> max(final double innerRadRatio, final double outerRadRatio) {
            return new RadiusSizer<>() {
                @Override
                public double getInnerRadius(final T data, final RectangleRange range) {
                    return Math.min(range.getMaxRectangle().width(), range.getMaxRectangle().height()) / 2.0 * innerRadRatio * 0.99999;
                }

                @Override
                public double getOuterRadius(final T data, final RectangleRange range) {
                    return Math.min(range.getMaxRectangle().width(), range.getMaxRectangle().height()) / 2.0 * outerRadRatio * 0.99999;
                }

                @Override
                public double getOuterHoverRadius(final T data, final RectangleRange range) {
                    return Math.min(range.getMaxRectangle().width(), range.getMaxRectangle().height()) / 2.0 * 0.99999;
                }

                @Override
                public Point2d getCenter(final T data, final RectangleRange range) {
                    return range.getMaxRectangle().lower().combine(range.getMaxRectangle().upper(), Double::sum).scale(0.5);
                }
            };
        }
    }

    public interface Section<T> {
        void setup(WidgetContext<T> context);

        void setTrapezoid(Trapezoid bounds, Trapezoid hoverBounds);

        void draw(WidgetRenderContext context);

        boolean handleEvent(WidgetEvent event);

        void postDraw(final MatrixStack stack, final VertexConsumerProvider vertexConsumers, Rectangle screenBounds);
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
            return stateUpdater.handleEvent(widgetContext.getData(), bounds, hoverBounds, event);
        }

        @Override
        public void postDraw(final MatrixStack stack, final VertexConsumerProvider vertexConsumers, final Rectangle screenBounds) {
            renderer.postDraw(widgetContext.getData(), stack, vertexConsumers, bounds, hoverBounds, screenBounds);
        }
    }

    public interface SectionStateUpdater<T> {
        boolean handleEvent(T data, Trapezoid bounds, Trapezoid hoverBounds, WidgetEvent event);

        default void resize(final Trapezoid bounds, final Trapezoid hoverBounds) {
        }

        static <T> SectionStateUpdater<T> basic(final Consumer<? super T> onClick, final BiConsumer<Boolean, ? super T> hoverUpdate, final Predicate<? super T> hovered) {
            return (data, bounds, hoverBounds, event) -> {
                if (event instanceof WidgetEvent.MouseMoveEvent mouseMove) {
                    final boolean isHovered = hovered.test(data);
                    final Trapezoid b = isHovered ? hoverBounds : bounds;
                    final Point2d mouse = mouseMove.end();
                    if (b.isIn(mouse.x(), mouse.y()) ^ isHovered) {
                        hoverUpdate.accept(!isHovered, data);
                    }
                } else if (event instanceof WidgetEvent.MousePressEvent mousePress) {
                    final boolean isHovered = hovered.test(data);
                    final Trapezoid b = isHovered ? hoverBounds : bounds;
                    final Point2d mouse = mousePress.point();
                    if (mousePress.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && b.isIn(mouse.x(), mouse.y())) {
                        onClick.accept(data);
                        return true;
                    }
                }
                return false;
            };
        }
    }

    public interface SectionRenderer<T> {
        void render(T data, WidgetRenderContext renderContext, Trapezoid bounds, Trapezoid hoverBounds);

        default void postDraw(final T data, final MatrixStack stack, final VertexConsumerProvider vertexConsumers, final Trapezoid bounds, final Trapezoid hoverBounds, final Rectangle screenBounds) {
        }

        static <T> SectionRenderer<T> empty() {
            return (data, renderContext, bounds, hoverBounds) -> {
            };
        }

        static <T> SectionRenderer<T> flat(final Predicate<? super T> hovered, final ToIntFunction<? super T> color, final Function<? super T, Optional<OrderedText>> textExtractor) {
            return (data, renderContext, bounds, hoverBounds) -> {
                final WidgetQuadEmitter emitter = renderContext.emitter();
                final Trapezoid trapezoid = hovered.test(data) ? hoverBounds : bounds;
                emitter.quad(trapezoid);
                final int c = color.applyAsInt(data);
                emitter.color(c, c, c, c);
                final int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
                emitter.light(l, l, l, l);
                emitter.sprite(TBCExV3GuiClient.FLAT_SPRITE_CACHE.getSprite());
                emitter.emit();
                final Optional<OrderedText> opt = textExtractor.apply(data);
                if (opt.isPresent()) {
                    final OrderedText text = opt.get();
                    final Point2d center = trapezoid.center();
                    final Point2d centerStart = trapezoid.getVertex(0).sum(trapezoid.getVertex(1)).scale(0.5);
                    final Point2d centerEnd = trapezoid.getVertex(2).sum(trapezoid.getVertex(3)).scale(0.5);
                    final Point2d delta = centerEnd.sum(centerStart.scale(-1));
                    double angle = Math.atan2(delta.y(), delta.x());
                    final boolean swap = centerStart.x() > centerEnd.x();
                    if (swap) {
                        angle = angle - Math.PI;
                    }
                    final double maxWidth = centerStart.distance(centerEnd);
                    final double width = renderContext.textEmitter().width(text);
                    final double scale = maxWidth / width;
                    renderContext.pushTransform(WidgetRenderContext.Transform.translate(center.x(), center.y()));
                    renderContext.pushTransform(WidgetRenderContext.Transform.rotate(angle));
                    final double off;
                    if (swap) {
                        off = -scale * MinecraftClient.getInstance().textRenderer.fontHeight;
                    } else {
                        off = 0;
                    }
                    renderContext.pushTransform(WidgetRenderContext.Transform.translate(-maxWidth / 2.0, off));
                    renderContext.pushTransform(WidgetRenderContext.Transform.scale(scale, scale));
                    renderContext.textEmitter().emit(text, 0, 0, -1, true);
                    renderContext.popTransform();
                    renderContext.popTransform();
                    renderContext.popTransform();
                    renderContext.popTransform();
                }
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
