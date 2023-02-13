package io.github.stuff_stuffs.tbcexv3model.api.animation;

public interface ModelAnimationFactory<T> {
    ModelAnimation<T> create(Context<T> context);

    interface Context<T> {
        T data();
    }
}
