package io.github.stuff_stuffs.tbcexv3_gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetQuadEmitter;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3_gui.internal.client.TBCExV3GuiClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

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
    }
}
