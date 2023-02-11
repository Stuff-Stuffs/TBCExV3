package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.impl.animation.AnimationManagerImpl;
import net.minecraft.util.Identifier;

public interface AnimationManager<T> {
    AnimationScene scene();

    ModelAnimationManager<T> forModel(Identifier id);

    void update(final double time, final T data);

    static <T> AnimationManager<T> create() {
        return new AnimationManagerImpl<>();
    }
}
