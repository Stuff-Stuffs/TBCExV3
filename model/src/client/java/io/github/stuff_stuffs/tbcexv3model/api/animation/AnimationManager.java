package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.impl.animation.AnimationManagerImpl;

import java.util.OptionalDouble;
import java.util.function.Consumer;

public interface AnimationManager<T> {
    OptionalDouble submit(Animation<T> animation, double after);

    void update(double time);

    AnimationScene scene();

    void forEach(Consumer<ScheduledAnimation<T>> consumer);

    static <T> AnimationManager<T> create() {
        return new AnimationManagerImpl<>();
    }
}
