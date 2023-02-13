package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

import java.util.Optional;
import java.util.function.BiConsumer;

public interface ActionTraceAnimator {
    Optional<BiConsumer<AnimationScene<BattleAnimationContext>, BattleStateView>> animate(TracerView.Node<ActionTrace> trace);
}
