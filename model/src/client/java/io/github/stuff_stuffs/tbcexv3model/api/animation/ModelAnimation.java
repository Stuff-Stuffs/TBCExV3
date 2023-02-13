package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelAnimationContext;

public interface ModelAnimation<T> {
    boolean animate(Model model, ModelAnimationContext<T> context, double time, double startTime);
}
