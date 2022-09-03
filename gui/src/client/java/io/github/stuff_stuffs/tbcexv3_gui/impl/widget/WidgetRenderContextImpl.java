package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Quadrilateral;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3_gui.internal.client.StencilFrameBuffer;
import io.github.stuff_stuffs.tbcexv3_gui.internal.client.TBCExV3GuiClient;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WidgetRenderContextImpl implements WidgetRenderContext {
    private final float time;
    private final Rectangle screenBounds;
    final Matrix4f screenSpaceMatrix;
    private final List<DrawnElement> elementsToDraw = new ArrayList<>();
    private final Map<RenderLayer, VertexDataArray> buildingElements = new Reference2ObjectOpenHashMap<>();
    private final DrawState drawState;


    public WidgetRenderContextImpl(final float time, final Rectangle screenBounds, final Matrix4f screenSpaceMatrix) {
        this.time = time;
        this.screenBounds = screenBounds;
        this.screenSpaceMatrix = screenSpaceMatrix;
        drawState = new DrawState();
        pushMatrix(screenSpaceMatrix);
    }

    @Override
    public float time() {
        return time;
    }

    @Override
    public WidgetRenderContext pushMatrix(final Matrix4f matrix) {
        return pushMatrix(matrix, 0);
    }

    @Override
    public WidgetRenderContext pushScissor(final Quadrilateral scissor) {
        return pushScissor(scissor, 0);
    }

    public WidgetRenderContext pushMatrix(final Matrix4f matrix, final int parentState) {
        return new WrappedWidgetRenderContextImpl(this, drawState.pushMatrix(matrix, parentState));
    }

    public WidgetRenderContext pushScissor(final Quadrilateral scissors, final int parentState) {
        return new WrappedWidgetRenderContextImpl(this, drawState.pushScissors(scissors, parentState));
    }

    @Override
    public VertexConsumer getVertexConsumer(final RenderLayer renderLayer) {
        if (!renderLayer.areVerticesNotShared()) {
            throw new IllegalArgumentException();
        }
        return new WidgetVertexConsumer(renderLayer, screenSpaceMatrix, (layer, data) -> submit(layer, data, 0));
    }

    public void draw() {
        if (elementsToDraw.size() == 0) {
            return;
        }
        elementsToDraw.sort(DrawnElement.COMPARATOR);
        final BufferBuilder builder = Tessellator.getInstance().getBuffer();
        int i = 0;
        int lastDrawState = elementsToDraw.get(i).drawState;
        while (i < elementsToDraw.size()) {
            final int drawState = elementsToDraw.get(i).drawState;
            if (drawState != lastDrawState) {
                copy(builder);
                lastDrawState = drawState;
            }
            i = i + renderRun(i, drawState, builder);
        }
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        copy(builder);
    }

    private void copy(final BufferBuilder builder) {
        final Matrix4f blitMat = screenSpaceMatrix;
        final Shader shader = MinecraftClient.getInstance().gameRenderer.blitScreenShader;
        TBCExV3GuiClient.getGuiFrameBuffer().beginRead();
        shader.addSampler("DiffuseSampler", TBCExV3GuiClient.getGuiFrameBuffer());
        final Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
        framebuffer.beginWrite(false);
        shader.bind();
        RenderSystem.setShader(() -> MinecraftClient.getInstance().gameRenderer.blitScreenShader);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
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

    private int renderRun(final int startIndex, final int matrixState, final BufferBuilder buffer) {
        final RenderLayer renderLayer;
        final int drawState;
        {
            final DrawnElement element = elementsToDraw.get(startIndex);
            renderLayer = element.renderLayer;
            drawState = element.drawState;
            setupStencil(buffer, drawState);
        }
        buffer.begin(renderLayer.getDrawMode(), renderLayer.getVertexFormat());
        DrawnElement element;
        int offset = 0;
        final VertexFormat.DrawMode drawMode = renderLayer.getDrawMode();
        final VertexFormat vertexFormat = renderLayer.getVertexFormat();
        final ImmutableList<VertexFormatElement> vertexFormatElements = vertexFormat.getElements();
        while (startIndex + offset < elementsToDraw.size() && (element = elementsToDraw.get(startIndex + offset)).renderLayer == renderLayer && element.drawState == drawState) {
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
                            case INT, UINT, FLOAT -> buffer.putFloat(k * 4, view.getFloat(i + k * 4));
                            case BYTE, UBYTE -> buffer.putByte(k, view.get(i + k));
                            case SHORT, USHORT -> buffer.putShort(k * 2, view.getShort(i + k * 2));
                        }
                    }
                    buffer.nextElement();
                    i = i + componentCount * componentSize;
                }
                buffer.next();
            }
            offset++;
        }
        renderLayer.draw(buffer, 0, 0, 0);
        return offset;
    }

    private void setupStencil(final BufferBuilder buffer, final int drawStateId) {
        DrawStateEntry entry = drawState.states.get(drawStateId);
        final DrawStateEntry[] entries = new DrawStateEntry[entry.depth + 1];
        int i = entries.length - 1;
        while (entry != null) {
            entries[i] = entry;
            entry = entry.previous;
            i--;
        }

        final Matrix4f mat = screenSpaceMatrix.copy();

        int count = 0;
        for (final DrawStateEntry drawStateEntry : entries) {
            if (drawStateEntry.matrix.isPresent()) {
                mat.multiply(drawStateEntry.matrix.get());
            }
            if (drawStateEntry.scissors.isPresent()) {
                if (count == 0) {
                    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
                    RenderSystem.setShader(GameRenderer::getPositionShader);
                }
                final Quadrilateral scissors = drawStateEntry.scissors.get();
                buffer.vertex(mat, (float) scissors.getVertexX(0), (float) scissors.getVertexY(0), 0).next();
                buffer.vertex(mat, (float) scissors.getVertexX(1), (float) scissors.getVertexY(1), 0).next();
                buffer.vertex(mat, (float) scissors.getVertexX(2), (float) scissors.getVertexY(2), 0).next();
                buffer.vertex(mat, (float) scissors.getVertexX(3), (float) scissors.getVertexY(3), 0).next();
                count++;
            }
        }
        if (count > 0) {
            final StencilFrameBuffer guiFrameBuffer = TBCExV3GuiClient.getGuiFrameBuffer();
            guiFrameBuffer.beginWrite(true);
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            GlStateManager._colorMask(false, false, false, false);
            GlStateManager._depthMask(false);
            GL11.glStencilMask(255);
            GL11.glClearStencil(0);
            GlStateManager._clear(GL11.GL_STENCIL_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            GL11.glStencilOp(GL11.GL_INCR, GL11.GL_INCR, GL11.GL_INCR);
            GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
            BufferRenderer.drawWithShader(buffer.end());
            guiFrameBuffer.endWrite();
            GlStateManager._colorMask(true, true, true, true);
            GlStateManager._depthMask(true);
            GL11.glStencilFunc(GL11.GL_EQUAL, count, 255);
            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        } else {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
        }
    }

    public Matrix4f getMatrix(final int drawStateId) {
        DrawStateEntry entry = drawState.states.get(drawStateId);
        final DrawStateEntry[] entries = new DrawStateEntry[entry.depth + 1];
        int i = entries.length - 1;
        while (entry != null) {
            entries[i] = entry;
            entry = entry.previous;
            i--;
        }
        final Matrix4f mat = screenSpaceMatrix.copy();
        for (final DrawStateEntry drawStateEntry : entries) {
            if (drawStateEntry.matrix.isPresent()) {
                mat.multiply(drawStateEntry.matrix.get());
            }
        }
        return mat;
    }

    private static final class VertexDataArray {
        private final byte[] bytes;
        private int index;

        private VertexDataArray(final int size, final int vertexCount) {
            bytes = new byte[size * (vertexCount - 1)];
        }
    }

    private static final class DrawState {
        private static final Matrix4f IDENTITY;
        private static final DrawStateEntry NONE;
        private final Int2ObjectMap<DrawStateEntry> states = new Int2ObjectOpenHashMap<>();
        private int nextId = 1;

        public DrawState() {
            states.put(0, NONE);
        }

        public int pushMatrix(final Matrix4f matrix, final int parentState) {
            final Matrix4f mat = matrix.copy();
            final DrawStateEntry top = states.get(parentState);
            states.put(nextId, new DrawStateEntry(Optional.of(mat), Optional.empty(), top));
            return nextId++;
        }

        public int pushScissors(final Quadrilateral scissors, final int parentState) {
            final DrawStateEntry top = states.get(parentState);
            states.put(nextId, new DrawStateEntry(Optional.empty(), Optional.of(scissors), top));
            return nextId++;
        }

        static {
            IDENTITY = new Matrix4f();
            IDENTITY.loadIdentity();
            NONE = new DrawStateEntry(Optional.of(IDENTITY), Optional.empty(), null);
        }
    }

    private static final class DrawStateEntry {
        private final Optional<Matrix4f> matrix;
        private final Optional<Quadrilateral> scissors;
        private final @Nullable DrawStateEntry previous;
        private final int depth;

        private DrawStateEntry(final Optional<Matrix4f> matrix, final Optional<Quadrilateral> scissors, @Nullable final DrawStateEntry previous) {
            this.matrix = matrix;
            this.scissors = scissors;
            this.previous = previous;
            if (previous == null) {
                depth = 0;
            } else {
                depth = previous.depth + 1;
            }
        }
    }

    public void submit(final RenderLayer layer, final byte[] data, final int drawState) {
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
                elementsToDraw.add(new DrawnElement(layer, consolidated, 0, drawState));
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
                elementsToDraw.add(new DrawnElement(layer, consolidated, depth, drawState));
            }
            vertexDataArray.index = 0;
        } else {
            System.arraycopy(data, 0, vertexDataArray.bytes, vertexDataArray.index * vertexSizeByte, vertexSizeByte);
            vertexDataArray.index++;
        }
    }
}
