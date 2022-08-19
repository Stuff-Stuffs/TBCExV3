package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface BattleParticipantEffect {
    void init(BattleParticipantState state, Tracer<ActionTrace> tracer);

    void deinit();

    BattleParticipantEffectType<?, ?> getType();
}
