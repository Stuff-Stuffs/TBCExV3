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

    static PreBattleParticipantDeathEvent convert(final View view) {
        return view::preDeath;
    }

    static PreBattleParticipantDeathEvent invoker(final PreBattleParticipantDeathEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, tracer) -> {
            enter.run();
            for (final PreBattleParticipantDeathEvent event : events) {
                event.preDeath(state, tracer);
            }
            exit.run();
        };
    }
}
