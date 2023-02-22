package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import io.github.stuff_stuffs.tbcexv3util.api.util.DiscretePhaseTracker;
import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectSortedMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface ModelAnimation {
    Identifier IDLE = TBCExV3Model.id("idle");
    Identifier ACTION = TBCExV3Model.id("action");
    Identifier ACTIVE = TBCExV3Model.id("active");
    Identifier SETUP = TBCExV3Model.id("setup");
    DiscretePhaseTracker ANIMATION_LAYER = Util.make(DiscretePhaseTracker.create(), tracker -> {
        tracker.addPhase(SETUP, Set.of(), Set.of());
        tracker.addPhase(IDLE, Set.of(SETUP), Set.of());
        tracker.addPhase(ACTION, Set.of(IDLE), Set.of());
        tracker.addPhase(ACTIVE, Set.of(ACTION), Set.of());
    });

    boolean beforeStart(double time);

    ModelAnimationKeyFrame current(double time);

    @Nullable ModelAnimationKeyFrame next(double time);

    static ModelAnimation of(final Collection<ModelAnimationKeyFrame> frames) {
        final Double2ObjectSortedMap<ModelAnimationKeyFrame> sorted = new Double2ObjectAVLTreeMap<>();
        for (final ModelAnimationKeyFrame frame : frames) {
            sorted.put(frame.start(), frame);
        }
        return new ModelAnimation() {
            @Override
            public boolean beforeStart(final double time) {
                return sorted.headMap(time).isEmpty();
            }

            @Override
            public ModelAnimationKeyFrame current(final double time) {
                final Double2ObjectSortedMap<ModelAnimationKeyFrame> map = sorted.headMap(Math.nextAfter(time, 1.0));
                if (map.isEmpty()) {
                    throw new RuntimeException();
                }
                return map.get(map.lastDoubleKey());
            }

            @Override
            public @Nullable ModelAnimationKeyFrame next(final double time) {
                final Double2ObjectSortedMap<ModelAnimationKeyFrame> map = sorted.tailMap(Math.nextAfter(time, 1.0));
                if (map.isEmpty()) {
                    return null;
                }
                return map.get(map.firstDoubleKey());
            }
        };
    }
}
