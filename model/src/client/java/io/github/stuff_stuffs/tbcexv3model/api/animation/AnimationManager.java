package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import net.minecraft.util.Identifier;

public interface AnimationManager<T> extends AnimationManagerView<T> {
    ModelAnimationManager<T> forModel(Identifier id);

    void update(final double time, final T data, AnimationScene<T> scene);
}
