package io.github.stuff_stuffs.tbcexv3_gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetQuadEmitter;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3_gui.internal.client.TBCExV3GuiClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public final class WidgetRenderUtils {
    public static <T> WidgetRenderUtils.Renderer<T> basicPanelTerminal(final int color) {
        return basicPanelTerminal(data -> color);
    }

    public static <T> WidgetRenderUtils.Renderer<T> basicPanelTerminal(final ToIntFunction<? super T> colourGetter) {
        return (data, context, bounds) -> drawRectangle(context.emitter(), bounds, colourGetter.applyAsInt(data));
    }

    public static void drawRectangle(final WidgetQuadEmitter emitter, final Rectangle rectangle, final int c) {
        emitter.quad(rectangle);
        emitter.color(c, c, c, c);
        final int l = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        emitter.light(l, l, l, l);
        emitter.sprite(TBCExV3GuiClient.FLAT_SPRITE_CACHE.getSprite());
        emitter.emit();
    }

    public static <T> WidgetRenderUtils.Renderer<T> centeredText(final ToIntFunction<? super T> colorGetter, final Predicate<? super T> shadow, final Function<? super T, OrderedText> textGetter) {
        return (data, renderContext, bounds) -> centeredText(renderContext, bounds, colorGetter.applyAsInt(data), shadow.test(data), textGetter.apply(data));
    }

    public static void centeredText(final WidgetRenderContext context, final Rectangle rectangle, final int c, final boolean shadow, final OrderedText text) {
        final double width = rectangle.width();
        final double textWidth = context.textEmitter().width(text);
        final double scale = width / textWidth;
        final Point2d start = rectangle.center().sum(new Point2d(-width / 2.0, 0));
        context.pushTransform(WidgetRenderContext.Transform.translate(start.x(), start.y()));
        context.pushTransform(WidgetRenderContext.Transform.scale(scale, scale));
        context.textEmitter().emit(text, 0, 0, c, shadow);
        context.popTransform();
        context.popTransform();
    }

    private WidgetRenderUtils() {
    }

    public interface Renderer<T> {
        void render(T data, WidgetRenderContext renderContext, Rectangle bounds);

        default void postDraw(final T data, final MatrixStack stack, final VertexConsumerProvider vertexConsumers) {
        }

        static <T> Renderer<T> empty() {
            return (data, renderContext, bounds) -> {
            };
        }

        @SafeVarargs
        static <T> Renderer<T> compound(final Renderer<? super T>... renderers) {
            return new Renderer<>() {
                @Override
                public void render(final T data, final WidgetRenderContext renderContext, final Rectangle bounds) {
                    for (final Renderer<? super T> renderer : renderers) {
                        renderer.render(data, renderContext, bounds);
                    }
                }

                @Override
                public void postDraw(final T data, final MatrixStack stack, final VertexConsumerProvider vertexConsumers) {
                    for (final Renderer<? super T> renderer : renderers) {
                        renderer.postDraw(data, stack, vertexConsumers);
                    }
                }
            };
        }
    }
}
