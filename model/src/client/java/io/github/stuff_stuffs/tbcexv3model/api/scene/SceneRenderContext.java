package io.github.stuff_stuffs.tbcexv3model.api.scene;

import io.github.stuff_stuffs.tbcexv3model.impl.scene.SceneRenderContextImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public interface SceneRenderContext<T> {
    MatrixStack matrices();

    VertexConsumerProvider vertexConsumers();

    Vec3d cameraPos();

    Quaternionfc cameraLook();

    AnimationScene scene();

    T context();

    static <T> SceneRenderContext<T> create(final MatrixStack matrices, final VertexConsumerProvider vertexConsumers, final Vec3d cameraPos, final Quaternionfc cameraLook, final AnimationScene scene, final T context) {
        return new SceneRenderContextImpl<>(matrices, vertexConsumers, cameraPos, new Quaternionf(cameraLook), scene, context);
    }
}
