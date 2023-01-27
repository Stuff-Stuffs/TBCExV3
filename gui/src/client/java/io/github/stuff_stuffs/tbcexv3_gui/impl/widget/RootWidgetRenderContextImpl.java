package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetQuadEmitter;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetTextEmitter;
import io.github.stuff_stuffs.tbcexv3_gui.internal.client.StencilFrameBuffer;
import io.github.stuff_stuffs.tbcexv3_gui.internal.client.TBCExV3GuiClient;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class RootWidgetRenderContextImpl implements AbstractWidgetRenderContext {
    private static final Comparator<EmittedQuad> COMPARATOR = Comparator.comparingDouble(EmittedQuad::z).thenComparing(quad -> quad.texture().map(Identifier::toString).orElse("")).thenComparingInt(EmittedQuad::scissorState);
    private final WidgetQuadEmitterImpl emitter = new WidgetQuadEmitterImpl(0, this::draw);
    private final ObjectArrayList<WidgetRenderContext.Transform> transforms = new ObjectArrayList<>();
    private final IntArrayList scissorStates = new IntArrayList();
    private final Int2ObjectMap<WidgetRenderContext.ScissorTransform[]> scissorStateMap = new Int2ObjectOpenHashMap<>();
    private final List<EmittedQuad> quads = new ArrayList<>();
    private final Matrix4f posMat;
    private final Rectangle screenBounds;
    private final float time;
    private final WidgetTextEmitter textEmitter;
    private int nextScissor = 0;
    private int children = 0;

    public RootWidgetRenderContextImpl(final Matrix4f posMat, final Rectangle screenBounds, final float time) {
        this.posMat = posMat;
        this.screenBounds = screenBounds;
        this.time = time;
        textEmitter = new WidgetTextEmitterImpl(this::draw, 0.1F);
    }

    @Override
    public float time() {
        return time;
    }

    @Override
    public Rectangle screenBounds() {
        return screenBounds;
    }

    @Override
    public WidgetQuadEmitter emitter() {
        return emitter;
    }

    @Override
    public void pushTransform(final WidgetRenderContext.Transform transform) {
        transforms.push(transform);
        if (transform instanceof ScissorTransform scissor) {
            scissorStates.push(createScissorState(scissorStates.isEmpty() ? -1 : scissorStates.peekInt(0), transformScissors(scissor)));
        }
    }

    @Override
    public void popTransform() {
        if (transforms.pop() instanceof WidgetRenderContext.ScissorTransform) {
            scissorStates.popInt();
        }
    }

    @Override
    public WidgetRenderContext child() {
        return new WidgetRenderContextImpl(children++ + 1, this);
    }

    @Override
    public WidgetTextEmitter textEmitter() {
        return textEmitter;
    }

    private WidgetRenderContext.ScissorTransform transformScissors(final WidgetRenderContext.ScissorTransform transform) {
        final Rectangle rect = transform.rectangle;
        final EmittedQuad emittedQuad = new EmittedQuad(0, new float[]{(float) rect.lower().x(), (float) rect.lower().x(), (float) rect.upper().x(), (float) rect.upper().x()}, new float[]{(float) rect.lower().y(), (float) rect.upper().y(), (float) rect.upper().y(), (float) rect.lower().y()}, new float[4], new float[4], new int[4], new int[4], false, Optional.empty());
        transformScissors(emittedQuad);
        final Point2d lower = new Point2d(emittedQuad.x(0), emittedQuad.y(0));
        final Point2d upper = new Point2d(emittedQuad.x(2), emittedQuad.y(2));
        return new WidgetRenderContext.ScissorTransform(new Rectangle(lower, upper));
    }

    @Override
    public void transformScissors(final EmittedQuad quad) {
        for (final WidgetRenderContext.Transform transform : transforms) {
            if (transform instanceof WidgetRenderContext.QuadTransform quadTransform) {
                quadTransform.transform(quad);
            }
        }
    }

    @Override
    public int createScissorState(final int parent, final ScissorTransform transform) {
        final int id = nextScissor++;
        if (parent == -1) {
            scissorStateMap.put(id, new WidgetRenderContext.ScissorTransform[]{transform});
            return id;
        }
        WidgetRenderContext.ScissorTransform[] transforms = scissorStateMap.get(parent);
        transforms = Arrays.copyOf(transforms, transforms.length + 1);
        transforms[transforms.length - 1] = transform;
        scissorStateMap.put(id, transforms);
        return id;
    }

    @Override
    public void draw(final EmittedQuad quad) {
        if (quad.scissorState() == -1 && !scissorStates.isEmpty()) {
            quad.scissorState(scissorStates.peekInt(0));
        }
        for (int i = transforms.size() - 1; i >= 0; i--) {
            final Transform transform = transforms.get(i);
            if (transform instanceof WidgetRenderContext.QuadTransform quadTransform) {
                quadTransform.transform(quad);
                if (!quad.clear()) {
                    return;
                }
            }
        }
        quads.add(quad);
    }

    public void draw() {
        if (quads.isEmpty()) {
            return;
        }
        quads.sort(COMPARATOR);
        int lastScissorState = -1;
        int i = 0;
        Optional<Identifier> texture = Optional.empty();
        final StencilFrameBuffer frameBuffer = TBCExV3GuiClient.getGuiFrameBuffer();
        frameBuffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        frameBuffer.beginWrite(true);
        final BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
        do {
            final EmittedQuad quad = quads.get(i);
            boolean drew = false;
            if (quad.scissorState() != lastScissorState) {
                lastScissorState = quad.scissorState();
                draw(buffer, texture, lastScissorState, false);
                drew = true;
            }
            if (!texture.equals(quad.texture())) {
                if (!drew) {
                    draw(buffer, texture, -2, false);
                }
                texture = quad.texture();
            }
            buffer.vertex(posMat, quad.x(0), quad.y(0), quad.z()).color(quad.color(1)).texture(quad.spriteU(0), quad.spriteV(0)).light(quad.light(0)).next();
            buffer.vertex(posMat, quad.x(1), quad.y(1), quad.z()).color(quad.color(1)).texture(quad.spriteU(1), quad.spriteV(1)).light(quad.light(1)).next();
            buffer.vertex(posMat, quad.x(2), quad.y(2), quad.z()).color(quad.color(2)).texture(quad.spriteU(2), quad.spriteV(2)).light(quad.light(2)).next();
            buffer.vertex(posMat, quad.x(3), quad.y(3), quad.z()).color(quad.color(3)).texture(quad.spriteU(3), quad.spriteV(3)).light(quad.light(3)).next();
            i++;
        } while (i < quads.size());
        draw(buffer, texture, -2, true);
        frameBuffer.endWrite();
        copy(buffer);
    }

    private void draw(final BufferBuilder buffer, final Optional<Identifier> texture, final int stencil, final boolean last) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapProgram);
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE_MINUS_DST_ALPHA, GlStateManager.DstFactor.ONE);
        GlStateManager._depthMask(false);
        RenderSystem._setShaderTexture(0, texture.orElse(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE));
        Tessellator.getInstance().draw();
        if (stencil != -2) {
            setupStencil(buffer, stencil);
        }
        if (!last) {
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
        } else {
            setupStencil(buffer, -1);
        }
    }

    private void copy(final BufferBuilder builder) {
        final Matrix4f blitMat = posMat;
        final ShaderProgram shader = MinecraftClient.getInstance().gameRenderer.blitScreenProgram;
        TBCExV3GuiClient.getGuiFrameBuffer().beginRead();
        shader.addSampler("DiffuseSampler", TBCExV3GuiClient.getGuiFrameBuffer());
        final Framebuffer framebuffer = MinecraftClient.getInstance().getFramebuffer();
        framebuffer.beginWrite(false);
        shader.bind();
        RenderSystem.setShader(() -> MinecraftClient.getInstance().gameRenderer.blitScreenProgram);
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableBlend();
        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.BLIT_SCREEN);
        builder.vertex(blitMat, (float) screenBounds.lower().x(), (float) screenBounds.lower().y(), 0).texture(0, 1).color(-1).next();
        builder.vertex(blitMat, (float) screenBounds.lower().x(), (float) screenBounds.upper().y(), 0).texture(0, 0).color(-1).next();
        builder.vertex(blitMat, (float) screenBounds.upper().x(), (float) screenBounds.upper().y(), 0).texture(1, 0).color(-1).next();
        builder.vertex(blitMat, (float) screenBounds.upper().x(), (float) screenBounds.lower().y(), 0).texture(1, 1).color(-1).next();
        BufferRenderer.drawWithGlobalProgram(builder.end());
        TBCExV3GuiClient.getGuiFrameBuffer().endRead();
        framebuffer.endWrite();
        TBCExV3GuiClient.getGuiFrameBuffer().clear(MinecraftClient.IS_SYSTEM_MAC);
    }

    private void setupStencil(final BufferBuilder buffer, final int scissorState) {
        if (scissorState == -1) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            return;
        }
        final WidgetRenderContext.ScissorTransform[] transforms = scissorStateMap.get(scissorState);
        if (transforms.length == 0) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            return;
        }
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        for (final WidgetRenderContext.ScissorTransform transform : transforms) {
            buffer.vertex(posMat, (float) transform.rectangle.getVertexX(0), (float) transform.rectangle.getVertexY(0), 0).next();
            buffer.vertex(posMat, (float) transform.rectangle.getVertexX(1), (float) transform.rectangle.getVertexY(1), 0).next();
            buffer.vertex(posMat, (float) transform.rectangle.getVertexX(2), (float) transform.rectangle.getVertexY(2), 0).next();
            buffer.vertex(posMat, (float) transform.rectangle.getVertexX(3), (float) transform.rectangle.getVertexY(3), 0).next();
        }
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GlStateManager._colorMask(false, false, false, false);
        GlStateManager._depthMask(false);
        GL11.glStencilMask(255);
        GL11.glClearStencil(0);
        GlStateManager._clear(GL11.GL_STENCIL_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
        GL11.glStencilOp(GL11.GL_INCR, GL11.GL_INCR, GL11.GL_INCR);
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 255);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        GlStateManager._colorMask(true, true, true, true);
        GlStateManager._depthMask(true);
        GL11.glStencilFunc(GL11.GL_EQUAL, transforms.length, 255);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }
}
