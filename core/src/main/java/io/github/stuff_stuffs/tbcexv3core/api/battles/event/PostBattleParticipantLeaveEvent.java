package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PostBattleParticipantLeaveEvent {
    void postParticipantLeaveEvent(BattleParticipantStateView state, BattleState battleState, BattleParticipantRemovalReason reason, Tracer<ActionTrace> tracer);

    interface View {
        void postParticipantLeaveEvent(BattleParticipantStateView state, BattleStateView battleStateView, BattleParticipantRemovalReason reason, TracerView<ActionTrace> tracer);
    }

    static PostBattleParticipantLeaveEvent convert(final PostBattleParticipantLeaveEvent.View view) {
        return view::postParticipantLeaveEvent;
    }

    static PostBattleParticipantLeaveEvent invoker(final PostBattleParticipantLeaveEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (handle, state, reason, tracer) -> {
            enter.run();
            for (final PostBattleParticipantLeaveEvent listener : listeners) {
                listener.postParticipantLeaveEvent(handle, state, reason, tracer);
            }
            exit.run();
        };
    }
}
