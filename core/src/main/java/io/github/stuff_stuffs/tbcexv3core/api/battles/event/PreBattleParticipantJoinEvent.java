package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PreBattleParticipantJoinEvent {
    boolean preBattleParticipantJoin(BattleParticipantState state, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleParticipantJoin(BattleParticipantStateView state, TracerView<ActionTrace> tracer);
    }
}
