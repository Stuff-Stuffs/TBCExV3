package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface PostBattleParticipantJoinEvent {
    void postBattleParticipantJoin(BattleParticipantState state, Tracer<ActionTrace> tracer);

    interface View {
        void postBattleParticipantJoin(BattleParticipantStateView state, Tracer<ActionTrace> tracer);
    }

    static PostBattleParticipantJoinEvent convert(final View view) {
        return view::postBattleParticipantJoin;
    }

    static PostBattleParticipantJoinEvent invoker(final PostBattleParticipantJoinEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, tracer) -> {
            enter.run();
            for (final PostBattleParticipantJoinEvent listener : listeners) {
                listener.postBattleParticipantJoin(state, tracer);
            }
            exit.run();
        };
    }
}
