package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PostBattleParticipantSetHealthEvent {
    void postSetHealth(BattleParticipantState state, double oldHealth, Tracer<ActionTrace> tracer);

    interface View {
        void postSetHealth(BattleParticipantStateView state, double oldHealth, TracerView<ActionTrace> tracer);
    }
}
