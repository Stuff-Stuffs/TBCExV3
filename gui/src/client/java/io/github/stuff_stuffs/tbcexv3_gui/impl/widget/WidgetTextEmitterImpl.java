package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetTextEmitter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.Matrix4f;

import java.util.function.Consumer;

public class WidgetTextEmitterImpl implements WidgetTextEmitter {
    private static final Matrix4f IDENTITY = new Matrix4f();

    static {
        IDENTITY.loadIdentity();
    }

    private final Consumer<EmittedQuad> consumer;
    private final float z;

    public WidgetTextEmitterImpl(final Consumer<EmittedQuad> consumer, final float z) {
        this.consumer = consumer;
        this.z = z;
    }

    @Override
    public void emit(final OrderedText text, final double x, final double y, final int color, final boolean shadow) {
        MinecraftClient.getInstance().textRenderer.draw(text, 0, 0, color, shadow, IDENTITY, new TextConsumerProvider(x, y), true, 0, LightmapTextureManager.MAX_LIGHT_COORDINATE);
    }

    private final class TextConsumerProvider implements VertexConsumerProvider {
        private final double x, y;

        private TextConsumerProvider(final double x, final double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public VertexConsumer getBuffer(final RenderLayer layer) {
            if (layer.getVertexFormat() != VertexFormats.POSITION_COLOR_TEXTURE_LIGHT) {
                throw new RuntimeException();
            }
            return new Wrapped(x, y);
        }
    }

    private final class Wrapped implements VertexConsumer {
        private final float x, y;
        private EmittedQuad building;
        private int index = 0;

        public Wrapped(final double x, final double y) {
            this.x = (float) x;
            this.y = (float) y;
            reset();
        }

        private void reset() {
            index = 0;
            building = new EmittedQuad(z, new float[4], new float[4], new float[4], new float[4], new int[4], new int[4], true);
        }

        @Override
        public VertexConsumer vertex(final double x, final double y, final double z) {
            building.x(index, this.x + (float) x);
            building.y(index, this.y + (float) y);
            return this;
        }

        @Override
        public VertexConsumer color(final int red, final int green, final int blue, final int alpha) {
            building.color(index, (alpha << 24) | ((red & 0xFF) << 16) | ((blue & 0xFF) << 8) | (green & 0xFF));
            return this;
        }

        @Override
        public VertexConsumer texture(final float u, final float v) {
            building.spriteU(index, u);
            building.spriteV(index, v);
            return this;
        }

        @Override
        public VertexConsumer overlay(final int u, final int v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public VertexConsumer light(final int u, final int v) {
            building.light(index, (u & 0xFFFF) | ((v & 0xFFFF) << 16));
            return this;
        }

        @Override
        public VertexConsumer normal(final float x, final float y, final float z) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void next() {
            index++;
            if (index == 4) {
                consumer.accept(building);
                reset();
            }
        }

        @Override
        public void fixedColor(final int red, final int green, final int blue, final int alpha) {
            throw new UnsupportedOperationException();

        }

        @Override
        public void unfixColor() {
            throw new UnsupportedOperationException();
        }
    }
}
