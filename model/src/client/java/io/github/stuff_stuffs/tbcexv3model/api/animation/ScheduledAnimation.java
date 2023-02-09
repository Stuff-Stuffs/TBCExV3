package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.scene.SceneRenderContext;

public interface ScheduledAnimation<T> {
    void render(SceneRenderContext<T> context, double time);
}
