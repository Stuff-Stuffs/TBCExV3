package io.github.stuff_stuffs.tbcexv3model.api.scene;

import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.animation.SceneAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpolation;
import io.github.stuff_stuffs.tbcexv3model.api.util.Transition;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.Set;

public interface AnimationSceneView<SC, MC> {
    Set<Identifier> models();

    void render(Identifier model, MatrixStack matrices, VertexConsumerProvider vertexConsumers);

    Model getModel(Identifier id);

    Optional<ModelAnimation> findModelAnimation(Identifier id, ModelType type, MC data, double offset);

    Optional<SceneAnimation> findSceneAnimation(Identifier id, SC data, double offset);

    Transition createTransition(Interpolation interpolation, double offset, double duration);
}
