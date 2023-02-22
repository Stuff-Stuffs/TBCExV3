package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationSceneView;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

import java.util.Optional;
import java.util.function.Consumer;

public interface ActionTraceAnimator {
    Optional<Consumer<AnimationScene<BattleSceneAnimationContext, BattleParticipantAnimationContext>>> animate(TracerView.Node<ActionTrace> trace, BattleAnimationContextFactory contextFactory, AnimationSceneView<BattleSceneAnimationContext, BattleParticipantAnimationContext> view);
}
