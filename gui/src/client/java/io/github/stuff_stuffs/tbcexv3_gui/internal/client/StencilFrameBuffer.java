package io.github.stuff_stuffs.tbcexv3_gui.internal.client;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL30;

public class StencilFrameBuffer extends Framebuffer {
    public StencilFrameBuffer(final int width, final int height, final boolean getError) {
        super(true);
        resize(width, height, getError);
    }

    @Override
    public void initFbo(final int width, final int height, final boolean getError) {
        RenderSystem.assertOnRenderThreadOrInit();
        final int i = RenderSystem.maxSupportedTextureSize();
        if (width > 0 && width <= i && height > 0 && height <= i) {
            viewportWidth = width;
            viewportHeight = height;
            textureWidth = width;
            textureHeight = height;
            fbo = GlStateManager.glGenFramebuffers();
            colorAttachment = TextureUtil.generateTextureId();
            if (useDepthAttachment) {
                depthAttachment = TextureUtil.generateTextureId();
                GlStateManager._bindTexture(depthAttachment);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_COMPARE_MODE, 0);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
                GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
                GlStateManager._texImage2D(GlConst.GL_TEXTURE_2D, 0, GL30.GL_DEPTH32F_STENCIL8, textureWidth, textureHeight, 0, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);
            }

            setTexFilter(GlConst.GL_NEAREST);
            GlStateManager._bindTexture(colorAttachment);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
            GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);
            GlStateManager._texImage2D(
                    GlConst.GL_TEXTURE_2D, 0, GlConst.GL_RGBA8, textureWidth, textureHeight, 0, GlConst.GL_RGBA, GlConst.GL_UNSIGNED_BYTE, null
            );
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fbo);
            GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, GlConst.GL_TEXTURE_2D, colorAttachment, 0);
            if (useDepthAttachment) {
                GlStateManager._glFramebufferTexture2D(GlConst.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GlConst.GL_TEXTURE_2D, depthAttachment, 0);
            }

            checkFramebufferStatus();
            clear(getError);
            endRead();
        } else {
            throw new IllegalArgumentException("Window " + width + "x" + height + " size out of bounds (max. size: " + i + ")");
        }
    }
}
