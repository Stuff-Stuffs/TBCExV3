package io.github.stuff_stuffs.tbcexv3model.api.scene;

import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.animation.SceneAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.api.util.Transition;
import io.github.stuff_stuffs.tbcexv3model.impl.AnimationSceneImpl;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.BlockRenderView;

import java.util.Optional;

public interface AnimationScene<SC, MC> extends AnimationSceneView<SC, MC> {
    void setSceneAnimation(Identifier layer, SceneAnimation animation, Transition transition);

    void setModelAnimation(Identifier modelId, Identifier layer, ModelAnimation animation, Transition transition);

    void addModel(Identifier id, ModelBuilder model, ModelType type);

    void removeModel(Identifier id);

    void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, BlockRenderView world, double time);

    void addSceneAnimationCallback(Identifier layer, Runnable runnable);

    static <SC, MC> AnimationScene<SC, MC> create(final double time) {
        return new AnimationSceneImpl<>(time);
    }

    static ModelBuilder modelBuilder() {
        return new AnimationSceneImpl.ModelBuilderImpl();
    }

    interface ModelBuilder {
        ModelBuilder addBone(Identifier id, Optional<Identifier> parentId);
    }
}
