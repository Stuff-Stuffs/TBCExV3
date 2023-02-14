package io.github.stuff_stuffs.tbcexv3model.api.scene;

import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManagerView;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelView;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionfc;

import java.util.Set;

public interface AnimationSceneView<T> {
    AnimationManagerView<T> manager();

    Set<Identifier> models();

    ModelView getModel(Identifier id);

    void render(MatrixStack matrices, VertexConsumerProvider vertexConsumer, Vec3d cameraPos, Quaternionfc cameraLook, double time);

    BufferToken upload(BufferBuilder.BuiltBuffer buffer);

    interface BufferToken {
        boolean isValid();

        VertexBuffer getBuffer();

        void destroy();
    }
}
