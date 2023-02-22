package io.github.stuff_stuffs.tbcexv3model.impl.animation;

import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.animation.SceneAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.animation.SceneAnimationKeyFrame;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.scene.property.ScenePropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpolation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class ReserveBuilderImpl implements SceneAnimation.ReserveBuilder {
    private final Map<Identifier, Map<Identifier, ModelAnimation>> animations = new Object2ObjectOpenHashMap<>();

    @Override
    public SceneAnimation.ReserveBuilder add(final Identifier modelId, final Identifier layer, final ModelAnimation animation) {
        animations.computeIfAbsent(modelId, i -> new Object2ObjectOpenHashMap<>()).put(layer, animation);
        return this;
    }

    @Override
    public SceneAnimation build() {
        final SceneAnimationKeyFrame start = SceneAnimationKeyFrame.simple(Double.NEGATIVE_INFINITY, ScenePropertyContainer.builder().build(), Interpolation.linear(), i -> {
        });
        double endTime = Double.NEGATIVE_INFINITY;
        for (final Map<Identifier, ModelAnimation> map : animations.values()) {
            for (final ModelAnimation animation : map.values()) {
                endTime = Math.max(endTime, animation.current(Double.POSITIVE_INFINITY).start());
            }
        }
        final SceneAnimationKeyFrame end = SceneAnimationKeyFrame.simple(endTime, ScenePropertyContainer.builder().build(), Interpolation.linear(), i -> {
        });
        return SceneAnimation.of(List.of(start, end));
    }

    @Override
    public void apply(final Runnable finishCallback, final AnimationScene<?, ?> scene, final Identifier layerId, final Interpolation interpolation, final double fadeInTime) {
        final SceneAnimation build = build();
        scene.addSceneAnimationCallback(layerId, () -> {
            scene.setSceneAnimation(layerId, build, scene.createTransition(interpolation, 0, fadeInTime));
            for (final Map.Entry<Identifier, Map<Identifier, ModelAnimation>> entry : animations.entrySet()) {
                for (final Map.Entry<Identifier, ModelAnimation> animationEntry : entry.getValue().entrySet()) {
                    scene.setModelAnimation(entry.getKey(), animationEntry.getKey(), animationEntry.getValue(), scene.createTransition(interpolation, 0, fadeInTime));
                }
            }
        });
        scene.addSceneAnimationCallback(layerId, finishCallback);
    }
}
