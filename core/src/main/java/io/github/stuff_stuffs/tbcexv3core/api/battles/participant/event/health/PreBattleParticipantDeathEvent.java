package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PreBattleParticipantDeathEvent {
    void preDeath(BattleParticipantState state, Tracer<ActionTrace> tracer);

    interface View {
        void preDeath(BattleParticipantStateView state, TracerView<ActionTrace> tracer);
    }
}
