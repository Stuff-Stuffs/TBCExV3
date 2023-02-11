package io.github.stuff_stuffs.tbcexv3core.impl.animation;

import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimator;
import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimatorRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;

public class ActionTraceAnimatorRegistryImpl implements ActionTraceAnimatorRegistry {
    private final Map<Identifier, List<PhasedActionTraceAnimator>> animators = new Object2ReferenceOpenHashMap<>();

    public ActionTraceAnimatorRegistryImpl() {
        PHASE_TRACKER.addListener(() -> {
            for (final List<PhasedActionTraceAnimator> list : animators.values()) {
                list.sort(PhasedActionTraceAnimator.COMPARATOR);
            }
        });
    }

    @Override
    public void register(final Identifier watch, final Identifier phase, final ActionTraceAnimator animator) {
        final List<PhasedActionTraceAnimator> list = animators.computeIfAbsent(watch, i -> new ArrayList<>());
        list.add(new PhasedActionTraceAnimator(phase, animator));
        list.sort(PhasedActionTraceAnimator.COMPARATOR);
    }

    @Override
    public Optional<Consumer<AnimationManager<BattleAnimationContext>>> animate(final TracerView.Node<ActionTrace> trace) {
        final Optional<Identifier> identifier = trace.value().animationData();
        if (identifier.isPresent()) {
            final List<PhasedActionTraceAnimator> animators = this.animators.get(identifier.get());
            if (animators == null) {
                return Optional.empty();
            }
            for (final PhasedActionTraceAnimator animator : animators) {
                final Optional<Consumer<AnimationManager<BattleAnimationContext>>> animation = animator.animator().animate(trace);
                if (animation.isPresent()) {
                    return animation;
                }
            }
        }
        return Optional.empty();
    }

    private record PhasedActionTraceAnimator(Identifier phase, ActionTraceAnimator animator) {
        public static final Comparator<PhasedActionTraceAnimator> COMPARATOR = Comparator.comparing(PhasedActionTraceAnimator::phase, ActionTraceAnimatorRegistry.PHASE_TRACKER.phaseComparator());
    }
}
