package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PostBattleParticipantSetBoundsEvent {
    void preSetBounds(BattleParticipantState state, BattleParticipantBounds oldBounds, Tracer<ActionTrace> tracer);

    interface View {
        void preSetBounds(BattleParticipantStateView state, BattleParticipantBounds oldBounds, TracerView<ActionTrace> tracer);
    }
}
