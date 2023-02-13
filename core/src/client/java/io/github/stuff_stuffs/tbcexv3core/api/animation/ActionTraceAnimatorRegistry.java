package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.impl.animation.ActionTraceAnimatorRegistryImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3util.api.util.DiscretePhaseTracker;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

public interface ActionTraceAnimatorRegistry {
    Identifier DEFAULT_PHASE = TBCExV3Core.createId("default");
    DiscretePhaseTracker PHASE_TRACKER = Util.make(DiscretePhaseTracker.create(), tracker -> tracker.addPhase(DEFAULT_PHASE, Set.of(), Set.of()));
    ActionTraceAnimatorRegistry INSTANCE = new ActionTraceAnimatorRegistryImpl();

    default void register(final Identifier watch, final ActionTraceAnimator animator) {
        register(watch, DEFAULT_PHASE, animator);
    }

    void register(Identifier watch, Identifier phase, ActionTraceAnimator animator);

    Optional<BiConsumer<AnimationScene<BattleAnimationContext>, BattleStateView>> animate(TracerView.Node<ActionTrace> trace);
}
