package io.github.stuff_stuffs.tbcexv3model.api.scene;

import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationFactory;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.impl.scene.AnimationSceneImpl;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface AnimationScene<T> extends AnimationSceneView<T>, AutoCloseable {
    @Override
    AnimationManager<T> manager();

    @Override
    Model getModel(Identifier id);

    void removeModel(Identifier id);

    void addModel(Identifier id, ModelType type, Map<Identifier, ModelAnimationFactory<T>> defaultFactories);

    void update(double time, T data);

    static <T> AnimationScene<T> create() {
        return new AnimationSceneImpl<>();
    }
}
