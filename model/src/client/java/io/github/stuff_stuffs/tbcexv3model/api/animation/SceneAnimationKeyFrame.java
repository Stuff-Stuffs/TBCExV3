package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.scene.property.ScenePropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpolation;

import java.util.function.Consumer;

public interface SceneAnimationKeyFrame {
    double start();

    ScenePropertyContainer properties();

    ScenePropertyContainer interpolatedProperties(double time, double duration, ScenePropertyContainer old, ScenePropertyContainer nextKeyFrame);

    void addOrRemoveModels(AnimationScene<?, ?> scene);

    static SceneAnimationKeyFrame simple(final double time, final ScenePropertyContainer properties, final Interpolation interpolation, final Consumer<AnimationScene<?, ?>> addOrRemoveModels) {
        return new SceneAnimationKeyFrame() {
            @Override
            public double start() {
                return time;
            }

            @Override
            public ScenePropertyContainer properties() {
                return properties;
            }

            @Override
            public ScenePropertyContainer interpolatedProperties(final double time, final double duration, final ScenePropertyContainer old, final ScenePropertyContainer nextKeyFrame) {
                final double relative = (time - start()) / duration;
                return ScenePropertyContainer.blend(ScenePropertyContainer.fallback(properties, old), nextKeyFrame, interpolation.remapAlpha(relative));
            }

            @Override
            public void addOrRemoveModels(final AnimationScene<?, ?> scene) {
                addOrRemoveModels.accept(scene);
            }
        };
    }
}
