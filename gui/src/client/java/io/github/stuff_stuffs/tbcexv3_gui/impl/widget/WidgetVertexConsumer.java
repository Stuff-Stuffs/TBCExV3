package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import io.github.stuff_stuffs.tbcexv3_gui.internal.client.TBCExV3GuiRenderLayers;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

import java.nio.ByteBuffer;

final class WidgetVertexConsumer implements BufferVertexConsumer {
    private final Vector4f vec = new Vector4f();
    private final RenderLayer layer;
    private final VertexFormat format;
    private final byte[] backing;
    private final ByteBuffer view;
    private final Matrix4f screenSpaceMatrix;
    private final Drawer drawer;
    private int vertexFormatIndex = 0;
    private int indexSum = 0;
    private boolean fixedColor = false;
    private int fixedR, fixedG, fixedB, fixedA;

    WidgetVertexConsumer(final RenderLayer layer, final Matrix4f screenSpaceMatrix, final Drawer drawer) {
        this.layer = TBCExV3GuiRenderLayers.getGuiRenderLayer(layer);
        format = this.layer.getVertexFormat();
        backing = new byte[format.getVertexSizeByte()];
        this.screenSpaceMatrix = screenSpaceMatrix;
        this.drawer = drawer;
        view = ByteBuffer.wrap(backing);
    }

    @Override
    public VertexFormatElement getCurrentElement() {
        return format.getElements().get(vertexFormatIndex);
    }

    @Override
    public void nextElement() {
        indexSum = indexSum + format.getElements().get(vertexFormatIndex).getByteLength();
        vertexFormatIndex = vertexFormatIndex + 1;
        if (vertexFormatIndex > format.getElements().size()) {
            throw new IllegalStateException();
        }
    }

    @Override
    public VertexConsumer vertex(final double x, final double y, final double z) {
        vec.set((float) x, (float) y, (float) z, 1);
        vec.transform(screenSpaceMatrix);
        return BufferVertexConsumer.super.vertex(vec.getX() / vec.getW(), vec.getY() / vec.getW(), vec.getZ() / vec.getW());
    }

    @Override
    public void putByte(final int index, final byte value) {
        view.put(index + indexSum, value);
    }

    @Override
    public void putShort(final int index, final short value) {
        view.putShort(index + indexSum, value);
    }

    @Override
    public void putFloat(final int index, final float value) {
        view.putFloat(index + indexSum, value);
    }

    @Override
    public VertexConsumer color(final int red, final int green, final int blue, final int alpha) {
        if (fixedColor) {
            return BufferVertexConsumer.super.color(fixedR, fixedG, fixedB, fixedA);
        }
        return BufferVertexConsumer.super.color(red, green, blue, alpha);
    }

    @Override
    public void next() {
        if (vertexFormatIndex != format.getElements().size()) {
            throw new IllegalStateException();
        } else {
            drawer.submit(layer, backing);
            indexSum = 0;
            vertexFormatIndex = 0;
        }
    }

    @Override
    public void fixedColor(final int red, final int green, final int blue, final int alpha) {
        fixedR = red;
        fixedG = green;
        fixedB = blue;
        fixedA = alpha;
        fixedColor = true;
    }

    @Override
    public void unfixColor() {
        fixedColor = false;
    }
}