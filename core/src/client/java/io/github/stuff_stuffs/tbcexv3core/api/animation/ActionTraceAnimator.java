package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

import java.util.Optional;
import java.util.function.Consumer;

public interface ActionTraceAnimator {
    Optional<Consumer<AnimationManager<BattleAnimationContext>>> animate(TracerView.Node<ActionTrace> trace);
}
