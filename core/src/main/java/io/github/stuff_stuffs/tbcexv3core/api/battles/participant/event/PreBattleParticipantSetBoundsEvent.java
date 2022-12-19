package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PreBattleParticipantSetBoundsEvent {
    boolean preSetBounds(BattleParticipantState state, BattleParticipantBounds newBounds, Tracer<ActionTrace> tracer);

    interface View {
        void preSetBounds(BattleParticipantStateView state, BattleParticipantBounds newBounds, TracerView<ActionTrace> tracer);
    }

    static PreBattleParticipantSetBoundsEvent convert(final PreBattleParticipantSetBoundsEvent.View view) {
        return (state, newBounds, tracer) -> {
            view.preSetBounds(state, newBounds, tracer);
            return true;
        };
    }

    static PreBattleParticipantSetBoundsEvent invoker(final PreBattleParticipantSetBoundsEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, newBounds, tracer) -> {
            boolean b = true;
            enter.run();
            for (final PreBattleParticipantSetBoundsEvent event : events) {
                b &= event.preSetBounds(state, newBounds, tracer);
            }
            exit.run();
            return b;
        };
    }
}
