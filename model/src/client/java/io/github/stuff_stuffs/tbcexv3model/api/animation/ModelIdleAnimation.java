package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelAnimationContext;
import org.jetbrains.annotations.Nullable;

public interface ModelIdleAnimation<T> {
    void animate(@Nullable Model model, ModelAnimationContext<T> context, double time, double timeSinceLastAnimation);
}
