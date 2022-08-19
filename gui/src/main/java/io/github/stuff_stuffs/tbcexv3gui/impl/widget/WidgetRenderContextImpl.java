package io.github.stuff_stuffs.tbcexv3gui.impl.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3gui.internal.client.TBCExV3GuiClient;
import io.github.stuff_stuffs.tbcexv3gui.internal.client.TBCExV3GuiRenderLayers;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WidgetRenderContextImpl implements WidgetRenderContext {
    private final float time;
    private final Rectangle screenBounds;
    private final Matrix4f screenSpaceMatrix;
    private final List<DrawnElement> elementsToDraw = new ArrayList<>();
    private final Map<RenderLayer, VertexDataArray> buildingElements = new Reference2ObjectOpenHashMap<>();
    private final ScissorStates scissorStates;
    private final MatrixStates matrixStates;


    public WidgetRenderContextImpl(final float time, final Rectangle screenBounds, final Matrix4f screenSpaceMatrix) {
        this.time = time;
        this.screenBounds = screenBounds;
        this.screenSpaceMatrix = screenSpaceMatrix;
        scissorStates = new ScissorStates(screenBounds);
        matrixStates = new MatrixStates();
    }

    @Override
    public float time() {
        return time;
    }

    @Override
    public void pushMatrix(final Matrix4f matrix) {
        matrixStates.push(matrix);
    }

    @Override
    public void popMatrix() {
        matrixStates.pop();
    }

    @Override
    public void pushScissor(final Rectangle scissor) {
        scissorStates.push(scissor);
    }

    @Override
    public void popScissor() {
        scissorStates.pop();
    }

    @Override
    public VertexConsumer getVertexConsumer(final RenderLayer layer) {
        if (!layer.areVerticesNotShared()) {
            throw new IllegalArgumentException();
        }
        return new ConsumerImpl(TBCExV3GuiRenderLayers.getGuiRenderLayer(layer), layer.getVertexFormat());
    }

    public void draw() {
        if (elementsToDraw.size() == 0) {
            return;
        }
        //GlStateManager._enableScissorTest();
        elementsToDraw.sort(DrawnElement.COMPARATOR);
        final BufferBuilder builder = Tessellator.getInstance().getBuffer();
        int i = 0;
        int lastMatrixState = elementsToDraw.get(i).matrixState;
        while (i < elementsToDraw.size()) {
            final int matrixState = elementsToDraw.get(i).matrixState;
            if (matrixState != lastMatrixState) {
                copy(lastMatrixState, builder);
                lastMatrixState = matrixState;
            }
            i = i + renderRun(i, matrixState, builder);
        }
        copy(lastMatrixState, builder);
        //GlStateManager._disableScissorTest();
    }

    private void copy(final int matrixState, final BufferBuilder builder) {
        final Matrix4f blitMat = screenSpaceMatrix.copy();
        blitMat.multiply(matrixStates.states.get(matrixState));
        final Shader shader = MinecraftClient.getInstance().gameRenderer.blitScreenShader;
        TBCExV3GuiClient.getGuiFrameBuffer().beginRead();
        shader.addSampler("DiffuseSampler", TBCExV3GuiClient.getGuiFrameBuffer());
        final Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
        framebuffer.beginWrite(false);
        shader.bind();
        RenderSystem.setShader(() -> MinecraftClient.getInstance().gameRenderer.blitScreenShader);
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.BLIT_SCREEN);


        builder.vertex(blitMat, (float) screenBounds.lower().x(), (float) screenBounds.lower().y(), 0).texture(0, 0).color(-1).next();
        builder.vertex(blitMat, (float) screenBounds.lower().x(), (float) screenBounds.upper().y(), 0).texture(0, 1).color(-1).next();
        builder.vertex(blitMat, (float) screenBounds.upper().x(), (float) screenBounds.upper().y(), 0).texture(1, 1).color(-1).next();
        builder.vertex(blitMat, (float) screenBounds.upper().x(), (float) screenBounds.lower().y(), 0).texture(1, 0).color(-1).next();
        BufferRenderer.drawWithShader(builder.end());
        TBCExV3GuiClient.getGuiFrameBuffer().endRead();

        framebuffer.endWrite();
        TBCExV3GuiClient.getGuiFrameBuffer().clear(MinecraftClient.IS_SYSTEM_MAC);
    }

    private int renderRun(final int startIndex, final int matrixState, final BufferBuilder builder) {
        int scissorState;
        final RenderLayer renderLayer;
        final Matrix4f matrix;
        {
            final DrawnElement element = elementsToDraw.get(startIndex);
            scissorState = element.scissorState;
            renderLayer = element.renderLayer;
            matrix = new Matrix4f();
            matrix.loadIdentity();
            updateScissors(scissorStates.states.get(scissorState), matrixState);
        }
        builder.begin(renderLayer.getDrawMode(), renderLayer.getVertexFormat());
        DrawnElement element;
        int offset = 0;
        final VertexFormat.DrawMode drawMode = renderLayer.getDrawMode();
        final VertexFormat vertexFormat = renderLayer.getVertexFormat();
        final ImmutableList<VertexFormatElement> vertexFormatElements = vertexFormat.getElements();
        while (startIndex + offset < elementsToDraw.size() && (element = elementsToDraw.get(startIndex + offset)).matrixState == matrixState && element.renderLayer == renderLayer) {
            if (element.scissorState != scissorState) {
                scissorState = element.scissorState;
                updateScissors(scissorStates.states.get(scissorState), matrixState);
            }
            int i = 0;
            final ByteBuffer view = ByteBuffer.wrap(element.data);
            for (int vertexCount = 0; vertexCount < drawMode.firstVertexCount; vertexCount++) {
                for (int j = 0; j < vertexFormatElements.size(); j++) {
                    final VertexFormatElement formatElement = vertexFormatElements.get(j);
                    final VertexFormatElement.ComponentType componentType = formatElement.getComponentType();
                    final int componentCount = formatElement.getComponentCount();
                    final int componentSize = componentType.getByteLength();
                    for (int k = 0; k < componentCount; k++) {
                        switch (componentType) {
                            case INT, UINT, FLOAT -> builder.putFloat(k * 4, view.getFloat(i + k * 4));
                            case BYTE, UBYTE -> builder.putByte(k, view.get(i + k));
                            case SHORT, USHORT -> builder.putShort(k*2, view.getShort(i + k * 2));
                        }
                    }
                    builder.nextElement();
                    i = i + componentCount * componentSize;
                }
                builder.next();
            }
            offset++;
        }
        renderLayer.draw(builder, 0, 0, 0);
        return offset;
    }

    private void updateScissors(final Rectangle rectangle, final int matrixState) {
        final Matrix4f matrix = matrixStates.states.get(matrixState).copy();
        matrix.multiply(screenSpaceMatrix);
        final Vector4f vec = new Vector4f();
        vec.set((float) rectangle.lower().x(), (float) rectangle.lower().y(), 0.0F, 1.0F);
        vec.transform(matrix);
        final int x0 = (int) (vec.getX() / vec.getW());
        final int y0 = (int) (vec.getY() / vec.getW());
        vec.set((float) rectangle.upper().x(), (float) rectangle.upper().y(), 0.0F, 1.0F);
        vec.transform(matrix);
        final int x1 = (int) (vec.getX() / vec.getW());
        final int y1 = (int) (vec.getY() / vec.getW());
        //RenderSystem.enableScissor(Math.min(x0, x1), Math.min(y0, y1), Math.max(x0, x1) - Math.min(x0, x1), Math.max(y0, y1) - Math.min(y0, y1));
    }

    private static final class VertexDataArray {
        private final byte[] bytes;
        private int index;

        private VertexDataArray(final int size, final int vertexCount) {
            bytes = new byte[size * (vertexCount - 1)];
        }
    }

    private static final class ScissorStates {
        private final IntStack stateIds = new IntArrayList();
        private final Stack<Rectangle> stateStack = new ObjectArrayList<>();
        private final Int2ObjectMap<Rectangle> states = new Int2ObjectOpenHashMap<>();
        private int nextId = 1;

        public ScissorStates(final Rectangle screenBounds) {
            stateIds.push(0);
            stateStack.push(screenBounds);
            states.put(0, screenBounds);
        }

        public void push(final Rectangle rect) {
            final Rectangle clipped = rect.clip(stateStack.top());
            stateIds.push(nextId);
            stateStack.push(clipped);
            states.put(nextId++, clipped);
        }

        public void pop() {
            stateIds.popInt();
            stateStack.pop();
        }
    }

    private static final class MatrixStates {
        private static final Matrix4f IDENTITY;
        private final IntStack stateIds = new IntArrayList();
        private final Stack<Matrix4f> stateStack = new ObjectArrayList<>();
        private final Int2ObjectMap<Matrix4f> states = new Int2ObjectOpenHashMap<>();
        private int nextId = 1;

        public MatrixStates() {
            stateIds.push(0);
            stateStack.push(IDENTITY);
            states.put(0, IDENTITY);
        }

        public void push(final Matrix4f matrix) {
            final Matrix4f mat = matrix.copy();
            final Matrix4f top = stateStack.top();
            mat.multiply(top);
            stateIds.push(nextId);
            stateStack.push(mat);
            states.put(nextId++, mat);
        }

        public void pop() {
            stateIds.popInt();
            stateStack.pop();
        }

        static {
            IDENTITY = new Matrix4f();
            IDENTITY.loadIdentity();
        }
    }

    public void submit(final RenderLayer layer, final byte[] data, final int scissorState, final int matrixState) {
        final VertexDataArray vertexDataArray = buildingElements.computeIfAbsent(layer, l -> new VertexDataArray(l.getVertexFormat().getVertexSizeByte(), l.getDrawMode().firstVertexCount));
        final VertexFormat vertexFormat = layer.getVertexFormat();
        final int vertexSizeByte = vertexFormat.getVertexSizeByte();
        final int firstVertexCount = layer.getDrawMode().firstVertexCount;
        if (vertexDataArray.index + 1 == firstVertexCount) {
            final byte[] consolidated = new byte[firstVertexCount * vertexSizeByte];
            System.arraycopy(vertexDataArray.bytes, 0, consolidated, 0, vertexDataArray.index * vertexSizeByte);
            System.arraycopy(data, 0, consolidated, vertexDataArray.index * vertexSizeByte, vertexSizeByte);
            final int posIdx = vertexFormat.getElements().indexOf(VertexFormats.POSITION_ELEMENT);
            if (posIdx < 0) {
                elementsToDraw.add(new DrawnElement(layer, consolidated, 0, scissorState, matrixState));
            } else {
                int offset = 0;
                for (int i = 0; i < posIdx; i++) {
                    offset = offset + vertexFormat.getElements().get(i).getByteLength();
                }
                double depth = 0;
                final ByteBuffer wrapped = ByteBuffer.wrap(consolidated);
                for (int i = 0; i < firstVertexCount; i++) {
                    depth = depth + wrapped.getFloat(i * vertexSizeByte + offset + 2 * Float.BYTES);
                }
                depth = depth / firstVertexCount;
                elementsToDraw.add(new DrawnElement(layer, consolidated, depth, scissorState, matrixState));
            }
            vertexDataArray.index = 0;
        } else {
            System.arraycopy(data, 0, vertexDataArray.bytes, vertexDataArray.index * vertexSizeByte, vertexSizeByte);
            vertexDataArray.index++;
        }
    }

    private final class ConsumerImpl implements BufferVertexConsumer {
        private final Vector4f vec = new Vector4f();
        private final RenderLayer layer;
        private final VertexFormat format;
        private final byte[] backing;
        private final ByteBuffer view;
        private int vertexFormatIndex = 0;
        private int indexSum = 0;
        private boolean fixedColor = false;
        private int fixedR, fixedG, fixedB, fixedA;

        private ConsumerImpl(final RenderLayer layer, final VertexFormat format) {
            this.layer = layer;
            this.format = format;
            backing = new byte[format.getVertexSizeByte()];
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
                submit(layer, backing, scissorStates.stateIds.topInt(), matrixStates.stateIds.topInt());
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
}
