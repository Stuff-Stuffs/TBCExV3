package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

import java.util.List;

public interface BattleParticipantEffect {
    void init(BattleParticipantState state, Tracer<ActionTrace> tracer);

    void deinit(Tracer<ActionTrace> tracer);

    BattleParticipantEffectType<?, ?> getType();

    List<BattleParticipantAction> getActions();
}
