package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.scene.SceneRenderContext;

import java.util.List;
import java.util.function.BiConsumer;

public interface Animation<T> {
    List<AnimationSupplied> supplied(double time);

    List<AnimationDependency> dependencies(double time, List<AnimationSupplied> supplied);

    ScheduledAnimation<T> schedule(double time, AnimationScene scene, List<AnimationDependency> dependencies);

    static <T> Animation<T> simpleSupplier(double time, BiConsumer<Model, SceneRenderContext<T>> consumer) {
        return new Animation<T>() {
            @Override
            public List<AnimationSupplied> supplied(double time) {
                return null;
            }

            @Override
            public List<AnimationDependency> dependencies(double time, List<AnimationSupplied> supplied) {
                return null;
            }

            @Override
            public ScheduledAnimation<T> schedule(double time, AnimationScene scene, List<AnimationDependency> dependencies) {
                return null;
            }
        }
    }
}
