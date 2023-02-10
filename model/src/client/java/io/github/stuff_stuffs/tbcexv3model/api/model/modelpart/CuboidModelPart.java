package io.github.stuff_stuffs.tbcexv3model.api.model.modelpart;

import io.github.stuff_stuffs.tbcexv3model.api.model.ModelRenderPartContext;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.*;

import java.lang.Math;
import java.util.Arrays;
import java.util.function.Function;

public class CuboidModelPart implements ModelPart {
    private final Face[] faces;
    private Identifier texture = SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
    private Function<Identifier, RenderLayer> renderLayer;

    public CuboidModelPart(final float width, final float height, final float depth, final int textureU, final int textureV, final int textureWidth, final int textureHeight) {
        this(width, height, depth, textureU, textureV, textureWidth, textureHeight, RenderLayer::getEntitySolid);
    }

    public CuboidModelPart(final float width, final float height, final float depth, final int textureU, final int textureV, final int textureWidth, final int textureHeight, final Function<Identifier, RenderLayer> layer) {
        renderLayer = layer;
        final float u = (float) textureU;
        final float uZ = textureU + depth;
        final float uZX = textureU + depth + width;
        final float uZXX = textureU + depth + width + width;
        final float uZZX = textureU + depth + width + depth;
        final float uZZZX = textureU + depth + width + depth + width;
        final float v = (float) textureV;
        final float vZ = textureV + depth;
        final float vZY = textureV + depth + height;
        final int[] colors = new int[]{-1, -1, -1, -1};
        final Vector3f p000 = new Vector3f(0, 0, 0);
        final Vector3f p100 = new Vector3f(width, 0, 0);
        final Vector3f p110 = new Vector3f(width, height, 0);
        final Vector3f p010 = new Vector3f(0, height, 0);
        final Vector3f p001 = new Vector3f(0, 0, depth);
        final Vector3f p101 = new Vector3f(width, 0, depth);
        final Vector3f p111 = new Vector3f(width, height, depth);
        final Vector3f p011 = new Vector3f(0, height, depth);
        faces = new Face[]{
                new Face(new Vector3fc[]{p101, p100, p110, p111}, Direction.EAST.getUnitVector(), colors, new Vector2fc[]{
                        new Vector2f(uZZX / textureWidth, vZ / textureHeight),
                        new Vector2f(uZX / textureWidth, vZ / textureHeight),
                        new Vector2f(uZX / textureWidth, vZY / textureHeight),
                        new Vector2f(uZZX / textureWidth, vZY / textureHeight)
                }),
                new Face(new Vector3fc[]{p000, p001, p011, p010}, Direction.WEST.getUnitVector(), colors, new Vector2fc[]{
                        new Vector2f(u / textureWidth, vZ / textureHeight),
                        new Vector2f(uZ / textureWidth, vZ / textureHeight),
                        new Vector2f(uZ / textureWidth, vZY / textureHeight),
                        new Vector2f(u / textureWidth, vZY / textureHeight)
                }),
                new Face(new Vector3fc[]{p101, p001, p000, p100}, Direction.DOWN.getUnitVector(), colors, new Vector2fc[]{
                        new Vector2f(uZX / textureWidth, v / textureHeight),
                        new Vector2f(uZ / textureWidth, v / textureHeight),
                        new Vector2f(uZ / textureWidth, vZ / textureHeight),
                        new Vector2f(uZX / textureWidth, vZ / textureHeight)
                }),
                new Face(new Vector3fc[]{p110, p010, p011, p111}, Direction.UP.getUnitVector(), colors, new Vector2fc[]{
                        new Vector2f(uZXX / textureWidth, vZ / textureHeight),
                        new Vector2f(uZX / textureWidth, vZ / textureHeight),
                        new Vector2f(uZX / textureWidth, v / textureHeight),
                        new Vector2f(uZXX / textureWidth, v / textureHeight)
                }),
                new Face(new Vector3fc[]{p100, p000, p010, p110}, Direction.NORTH.getUnitVector(), colors, new Vector2fc[]{
                        new Vector2f(uZX / textureWidth, vZ / textureHeight),
                        new Vector2f(uZ / textureWidth, vZ / textureHeight),
                        new Vector2f(uZ / textureWidth, vZY / textureHeight),
                        new Vector2f(uZX / textureWidth, vZY / textureHeight)
                }),
                new Face(new Vector3fc[]{p001, p101, p111, p001}, Direction.SOUTH.getUnitVector(), colors, new Vector2fc[]{
                        new Vector2f(uZZZX / textureWidth, vZ / textureHeight),
                        new Vector2f(uZZX / textureWidth, vZ / textureHeight),
                        new Vector2f(uZZX / textureWidth, vZY / textureHeight),
                        new Vector2f(uZZZX / textureWidth, vZY / textureHeight)
                })
        };
    }

    @Override
    public void render(final ModelRenderPartContext context, double time) {
        final MatrixStack matrices = context.matrices();
        final Vector4f scratch = new Vector4f();
        final Vector3f normScratch = new Vector3f();
        final Vector4f center = new Vector4f();
        final Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        final Matrix3f normalMatrix = matrices.peek().getNormalMatrix();
        final VertexConsumer consumer = context.vertexConsumers().getBuffer(renderLayer.apply(texture));
        for (final Face face : faces) {
            normScratch.set(face.normal);
            normalMatrix.transform(normScratch);
            center.set(0.0F, 0.0F, 0.0F, 1.0F);
            for (final Vector3fc vertex : face.vertices) {
                center.add(vertex.x(), vertex.y(), vertex.z(), 0.0F);
            }
            center.mul(0.25F);
            for (int i = 0; i < 4; i++) {
                final Vector3fc vertex = face.vertices[i];
                scratch.set(vertex.x(), vertex.y(), vertex.z(), 1.0F);
                positionMatrix.transform(scratch);
                if (Math.abs(scratch.w()) < 0.0000001) {
                    continue;
                }
                scratch.mul(1 / scratch.w());
                final Vector2fc uv = face.uvs[i];
                consumer.vertex(scratch.x, scratch.y, scratch.z).color(face.colors[i]).texture(uv.x(), uv.y()).overlay(OverlayTexture.DEFAULT_UV).light(context.sampleLight(center.x(), center.y(), center.z())).normal(normScratch.x(), normScratch.y(), normScratch.z()).next();
            }
        }
    }

    public void setTexture(final Identifier texture) {
        this.texture = texture;
    }

    public void setRenderLayer(final Function<Identifier, RenderLayer> renderLayer) {
        this.renderLayer = renderLayer;
    }

    public void setUV(final Direction face, final int vertexIndex, final float u, final float v) {
        faces[face.getId()] = faces[face.getId()].setUV(vertexIndex, u, v);
    }

    public void setColor(final Direction face, final int vertexIndex, final int color) {
        faces[face.getId()] = faces[face.getId()].setColor(vertexIndex, color);
    }

    public static final class Face {
        private final Vector3fc[] vertices;
        private final Vector3fc normal;
        private final int[] colors;
        private final Vector2fc[] uvs;

        private Face(final Vector3fc[] vertices, final Vector3fc normal, final int[] colors, final Vector2fc[] uvs) {
            this.vertices = vertices;
            this.normal = normal;
            this.colors = colors;
            this.uvs = uvs;
        }

        public Face setUV(final int vertexIndex, final float u, final float v) {
            final Vector2fc[] uvs = Arrays.copyOf(this.uvs, this.uvs.length);
            uvs[vertexIndex] = new Vector2f(u, v);
            return new Face(vertices, normal, colors, uvs);
        }

        public Face setColor(final int vertexIndex, final int color) {
            final int[] colors = Arrays.copyOf(this.colors, this.colors.length);
            colors[vertexIndex] = color;
            return new Face(vertices, normal, colors, uvs);
        }
    }
}
