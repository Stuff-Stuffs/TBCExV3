package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;

public interface SceneAnimation<T> {
    boolean update(AnimationScene<T> scene, double time, double startTime);
}
