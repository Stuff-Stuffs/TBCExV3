package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3gui.api.Point2d;
import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public final class WidgetRenderUtils {
    public static <T> WidgetRenderUtils.Renderer<T> basicPanelTerminal(final int color, final RenderLayer renderLayer) {
        return basicPanelTerminal(data -> color, date -> renderLayer);
    }

    public static <T> WidgetRenderUtils.Renderer<T> basicPanelTerminal(final ToIntFunction<? super T> colourGetter, final RenderLayer renderLayer) {
        return basicPanelTerminal(colourGetter, date -> renderLayer);
    }

    public static <T> WidgetRenderUtils.Renderer<T> basicPanelTerminal(final ToIntFunction<? super T> colourGetter, final Function<? super T, RenderLayer> renderLayer) {
        return (data, context, bounds) -> drawRectangle(context.getVertexConsumer(renderLayer.apply(data)), bounds, colourGetter.applyAsInt(data));
    }

    public static void drawRectangle(final VertexConsumer vertexConsumer, final Rectangle rectangle, final int c) {
        final Point2d lower = rectangle.lower();
        final Point2d upper = rectangle.upper();
        vertexConsumer.vertex(lower.x(), lower.y(), 0).color(c).next();
        vertexConsumer.vertex(lower.x(), upper.y(), 0).color(c).next();
        vertexConsumer.vertex(upper.x(), upper.y(), 0).color(c).next();
        vertexConsumer.vertex(upper.x(), lower.y(), 0).color(c).next();
    }

    private WidgetRenderUtils() {
    }

    public interface Renderer<T> {
        void render(T data, WidgetRenderContext renderContext, Rectangle bounds);
    }
}
