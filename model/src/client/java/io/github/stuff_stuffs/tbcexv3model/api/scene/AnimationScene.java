package io.github.stuff_stuffs.tbcexv3model.api.scene;

import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationFactory;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.impl.scene.AnimationSceneImpl;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionfc;

import java.util.Map;
import java.util.Set;

public interface AnimationScene<T> extends AutoCloseable {
    AnimationManager<T> animationManager();

    BufferToken upload(BufferBuilder.BuiltBuffer buffer);

    Set<Identifier> models();

    Model getModel(Identifier id);

    void removeModel(Identifier id);

    void addModel(Identifier id, ModelType type, Map<Identifier, ModelAnimationFactory<T>> defaultFactories);

    void render(MatrixStack matrices, VertexConsumerProvider vertexConsumer, Vec3d cameraPos, Quaternionfc cameraLook, double time);

    void update(double time, T data);

    interface BufferToken {
        boolean isValid();

        VertexBuffer getBuffer();

        void destroy();
    }

    static <T> AnimationScene<T> create() {
        return new AnimationSceneImpl<>();
    }
}
