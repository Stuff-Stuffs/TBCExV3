package io.github.stuff_stuffs.tbcexv3core.api.battle.event;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface PreBattleParticipantJoinEvent {
    boolean preBattleParticipantJoin(BattleParticipantState state, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleParticipantJoin(BattleParticipantStateView state, Tracer<ActionTrace> tracer);
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
