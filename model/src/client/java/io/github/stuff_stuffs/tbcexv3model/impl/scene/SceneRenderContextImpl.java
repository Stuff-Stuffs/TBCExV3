package io.github.stuff_stuffs.tbcexv3model.impl.scene;

import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.scene.SceneRenderContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionfc;

public record SceneRenderContextImpl<T>(
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        Vec3d cameraPos,
        Quaternionfc cameraLook,
        AnimationScene scene,
        T context
) implements SceneRenderContext<T> {
}
