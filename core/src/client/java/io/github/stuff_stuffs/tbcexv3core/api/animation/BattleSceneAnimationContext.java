package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface BattleSceneAnimationContext {
    BattleAnimationContextFactory parent();

    TracerView.Node<ActionTrace> action();
}
