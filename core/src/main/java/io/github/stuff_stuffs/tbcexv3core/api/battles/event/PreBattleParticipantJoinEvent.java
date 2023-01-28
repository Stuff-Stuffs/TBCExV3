package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PreBattleParticipantJoinEvent {
    boolean preBattleParticipantJoin(BattleParticipantState state, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleParticipantJoin(BattleParticipantStateView state, TracerView<ActionTrace> tracer);
    }

    static PreBattleParticipantJoinEvent convert(final View view) {
        return (state, tracer) -> {
            view.preBattleParticipantJoin(state, tracer);
            return true;
        };
    }

    static PreBattleParticipantJoinEvent invoker(final PreBattleParticipantJoinEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, tracer) -> {
            enter.run();
            boolean accepted = true;
            for (final PreBattleParticipantJoinEvent listener : listeners) {
                accepted &= listener.preBattleParticipantJoin(state, tracer);
            }
            exit.run();
            return accepted;
        };
    }
}
