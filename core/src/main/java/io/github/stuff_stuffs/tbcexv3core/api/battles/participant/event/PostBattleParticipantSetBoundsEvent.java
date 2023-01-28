package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PostBattleParticipantSetBoundsEvent {
    void preSetBounds(BattleParticipantState state, BattleParticipantBounds oldBounds, Tracer<ActionTrace> tracer);

    interface View {
        void preSetBounds(BattleParticipantStateView state, BattleParticipantBounds oldBounds, TracerView<ActionTrace> tracer);
    }

    static PostBattleParticipantSetBoundsEvent convert(final PostBattleParticipantSetBoundsEvent.View view) {
        return view::preSetBounds;
    }

    static PostBattleParticipantSetBoundsEvent invoker(final PostBattleParticipantSetBoundsEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, oldBounds, tracer) -> {
            enter.run();
            for (final PostBattleParticipantSetBoundsEvent event : events) {
                event.preSetBounds(state, oldBounds, tracer);
            }
            exit.run();
        };
    }
}
