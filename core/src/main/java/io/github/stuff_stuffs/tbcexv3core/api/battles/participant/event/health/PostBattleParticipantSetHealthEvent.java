package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PostBattleParticipantSetHealthEvent {
    void postSetHealth(BattleParticipantState state, double oldHealth, Tracer<ActionTrace> tracer);

    interface View {
        void postSetHealth(BattleParticipantStateView state, double oldHealth, TracerView<ActionTrace> tracer);
    }

    static PostBattleParticipantSetHealthEvent convert(final View view) {
        return view::postSetHealth;
    }

    static PostBattleParticipantSetHealthEvent invoker(final PostBattleParticipantSetHealthEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, oldHealth, tracer) -> {
            enter.run();
            for (final PostBattleParticipantSetHealthEvent event : events) {
                event.postSetHealth(state, oldHealth, tracer);
            }
            exit.run();
        };
    }
}
