package io.github.stuff_stuffs.tbcexv3model.api.animation;

import java.util.function.Consumer;

public interface ModelAnimationManager<T> {
    void setIdleAnimation(ModelIdleAnimation<T> modelAnimation);

    boolean open();

    boolean setAnimation(ModelAnimation<T> animation);

    void enqueueListener(Listener<T> listener);

    interface Listener<T> {
        void onOpen(Consumer<ModelAnimation<T>> consumer, Runnable giveUpSlot);
    }
}
