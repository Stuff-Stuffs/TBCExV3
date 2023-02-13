package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface ModelAnimationManager<T> {
    ModelType type();

    @Nullable ModelAnimationFactory<T> get(Identifier animationId);

    void setModelAnimation(Identifier animationId, ModelAnimationFactory<T> animation);

    void animate(ModelAnimation<T> animation);

    boolean isDone();
}
