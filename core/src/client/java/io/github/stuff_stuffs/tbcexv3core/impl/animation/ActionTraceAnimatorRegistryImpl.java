package io.github.stuff_stuffs.tbcexv3core.impl.animation;

import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimator;
import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimatorRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ActionTraceAnimatorRegistryImpl implements ActionTraceAnimatorRegistry {
    private final Map<Identifier, List<ActionTraceAnimator>> animators = new Object2ReferenceOpenHashMap<>();

    @Override
    public void register(final Identifier watch, final ActionTraceAnimator animator) {
        animators.computeIfAbsent(watch, i -> new ArrayList<>()).add(animator);
    }

    @Override
    public Optional<Consumer<AnimationManager<BattleAnimationContext>>> animate(final TracerView.Node<ActionTrace> trace) {
        final Optional<Identifier> identifier = trace.value().animationData();
        if (identifier.isPresent()) {
            final List<ActionTraceAnimator> animators = this.animators.get(identifier.get());
            if (animators == null) {
                return Optional.empty();
            }
            for (final ActionTraceAnimator animator : animators) {
                final Optional<Consumer<AnimationManager<BattleAnimationContext>>> animation = animator.animate(trace);
                if (animation.isPresent()) {
                    return animation;
                }
            }
        }
        return Optional.empty();
    }
}
