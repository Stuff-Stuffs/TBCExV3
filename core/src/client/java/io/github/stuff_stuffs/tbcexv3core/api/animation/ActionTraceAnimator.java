package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3model.api.animation.Animation;

import java.util.Optional;

public interface ActionTraceAnimator {
    Optional<Animation<BattleAnimationContext>> animate(TracerView.Node<ActionTrace> trace);
}
