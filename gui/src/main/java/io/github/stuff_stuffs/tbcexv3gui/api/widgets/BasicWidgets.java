package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3gui.api.Point2d;
import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.Sizer;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class BasicWidgets {
    public static <T> AbstractSingleChildWidget<T> hide(final Predicate<? super T> visibility) {
        return new SingleAnimationWidget<>(new SingleAnimationWidget.Animation<>() {
            @Override
            public Optional<WidgetRenderContext> animate(final T data, final WidgetRenderContext parent) {
                return visibility.test(data) ? Optional.of(parent) : Optional.empty();
            }

            @Override
            public Optional<WidgetEvent> animateEvent(final T data, final WidgetEvent event) {
                return visibility.test(data) ? Optional.of(event) : Optional.empty();
            }
        });
    }

    public static <T> AbstractSingleChildWidget<T> button(final Function<? super T, ? extends ButtonData> stateGetter, final Sizer sizer) {
        return new AbstractSingleChildWidget<>() {
            @Override
            public boolean handleEvent(final WidgetEvent event) {
                if (super.handleEvent(event)) {
                    return true;
                }
                final WidgetContext<T> widgetContext = getWidgetContext();
                if (widgetContext == null) {
                    throw new NullPointerException();
                }
                final ButtonData buttonData = stateGetter.apply(widgetContext.getData());
                if (event instanceof WidgetEvent.MouseMoveEvent mouseMoveEvent) {
                    final Rectangle bounds = buttonData.bounds();
                    if (!bounds.contains(mouseMoveEvent.start()) && bounds.contains(mouseMoveEvent.end())) {
                        buttonData.setState(ButtonState.HOVER);
                    } else {
                        buttonData.setState(ButtonState.DEFAULT);
                    }
                } else if (event instanceof WidgetEvent.MousePressEvent mousePressEvent) {
                    final Rectangle bounds = buttonData.bounds();
                    if (mousePressEvent.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && bounds.contains(mousePressEvent.point())) {
                        buttonData.setState(ButtonState.PRESSED);
                        return true;
                    }
                } else if (event instanceof WidgetEvent.TickEvent) {
                    if (buttonData.state() == ButtonState.PRESSED) {
                        buttonData.setState(ButtonState.HOVER);
                    }
                }
                return true;
            }

            @Override
            public Rectangle resize(final Rectangle min, final Rectangle max) {
                final Rectangle bounds = sizer.calculateSize(min, max);
                final WidgetInfo<T, ?> child = getChild();
                if (child == null) {
                    throw new NullPointerException();
                }
                child.widget.resize(min, bounds);
                final WidgetContext<T> widgetContext = getWidgetContext();
                if (widgetContext == null) {
                    throw new NullPointerException();
                }
                stateGetter.apply(widgetContext.getData()).setBounds(bounds);
                return bounds;
            }

            @Override
            protected void drawSelf(final WidgetRenderContext context) {

            }
        };
    }

    public static Widget<Void> basicPanel(final int color, final RenderLayer renderLayer, final Sizer sizer) {
        return basicPanel(v -> color, renderLayer, sizer);
    }

    public static <T> Widget<T> basicPanel(final ToIntFunction<? super T> colourGetter, final RenderLayer renderLayer, final Sizer sizer) {
        return new TerminalWidget<>(new TerminalWidget.StateUpdater<T>() {
            @Override
            public boolean event(final WidgetEvent event, final T data) {
                return false;
            }

            @Override
            public void updateBounds(final Rectangle bounds, final T data) {

            }
        }, basicPanelTerminal(colourGetter, renderLayer), sizer);
    }

    private static <T> TerminalWidget.Renderer<T> basicPanelTerminal(final ToIntFunction<? super T> colourGetter, final RenderLayer renderLayer) {
        return (context, data, bounds) -> drawRectangle(context.getVertexConsumer(renderLayer), bounds, colourGetter.applyAsInt(data));
    }

    public static void drawRectangle(final VertexConsumer vertexConsumer, final Rectangle rectangle, final int c) {
        final Point2d lower = rectangle.lower();
        final Point2d upper = rectangle.upper();
        vertexConsumer.vertex(lower.x(), lower.y(),  0).color(c).next();
        vertexConsumer.vertex(lower.x(), upper.y(),  0).color(c).next();
        vertexConsumer.vertex(upper.x(), upper.y(),  0).color(c).next();
        vertexConsumer.vertex(upper.x(), lower.y(),  0).color(c).next();
    }

    public enum ButtonState {
        DEFAULT,
        HOVER,
        PRESSED
    }

    public interface ButtonData {
        void setState(ButtonState state);

        ButtonState state();

        void setBounds(Rectangle bounds);

        Rectangle bounds();
    }

    private BasicWidgets() {
    }
}
