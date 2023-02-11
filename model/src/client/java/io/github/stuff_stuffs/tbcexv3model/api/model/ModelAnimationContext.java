package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;

public interface ModelAnimationContext<T> {
    T context();

    AnimationScene scene();
}
