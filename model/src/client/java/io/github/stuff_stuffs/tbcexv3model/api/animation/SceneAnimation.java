package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpolation;
import io.github.stuff_stuffs.tbcexv3model.impl.animation.ReserveBuilderImpl;
import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import io.github.stuff_stuffs.tbcexv3util.api.util.DiscretePhaseTracker;
import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface SceneAnimation {
    Identifier PASSIVE = TBCExV3Model.id("idle");
    Identifier ACTIVE = TBCExV3Model.id("action");
    DiscretePhaseTracker ANIMATION_LAYER = Util.make(DiscretePhaseTracker.create(), tracker -> {
        tracker.addPhase(PASSIVE, Set.of(), Set.of());
        tracker.addPhase(ACTIVE, Set.of(PASSIVE), Set.of());
    });

    boolean beforeStart(double time);

    SceneAnimationKeyFrame current(double time);

    @Nullable SceneAnimationKeyFrame next(double time);

    static SceneAnimation of(final Collection<SceneAnimationKeyFrame> frames) {
        final Double2ObjectSortedMap<SceneAnimationKeyFrame> sorted = new Double2ObjectAVLTreeMap<>();
        for (final SceneAnimationKeyFrame frame : frames) {
            sorted.put(frame.start(), frame);
        }
        return new SceneAnimation() {
            @Override
            public boolean beforeStart(final double time) {
                return sorted.headMap(time).isEmpty();
            }

            @Override
            public SceneAnimationKeyFrame current(final double time) {
                final Double2ObjectSortedMap<SceneAnimationKeyFrame> map = sorted.headMap(Math.nextAfter(time, 1.0));
                if (map.isEmpty()) {
                    throw new RuntimeException();
                }
                return map.get(map.lastDoubleKey());
            }

            @Override
            public @Nullable SceneAnimationKeyFrame next(final double time) {
                final Double2ObjectSortedMap<SceneAnimationKeyFrame> map = sorted.tailMap(Math.nextAfter(time, 1.0));
                if (map.isEmpty()) {
                    return null;
                }
                return map.get(map.firstDoubleKey());
            }
        };
    }

    static ReserveBuilder reservationBuilder() {
        return new ReserveBuilderImpl();
    }

    interface ReserveBuilder {
        ReserveBuilder add(Identifier modelId, Identifier layer, ModelAnimation animation);

        SceneAnimation build();

        void apply(Runnable finishCallback, AnimationScene<?, ?> scene, Identifier layerId, Interpolation interpolation, double fadeInTime);
    }
}
