package io.github.stuff_stuffs.tbcexv3core.api.animation;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface BattleParticipantAnimationContext {
    BattleAnimationContextFactory parent();

    BattleParticipantHandle handle();

    TracerView.Node<ActionTrace> action();
}
