package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PreBattleParticipantLeaveEvent {
    boolean preParticipantLeaveEvent(BattleParticipantHandle handle, BattleState battleState, BattleParticipantRemovalReason reason, Tracer<ActionTrace> tracer);

    interface View {
        void preParticipantLeaveEvent(BattleParticipantHandle handle, BattleStateView battleStateView, BattleParticipantRemovalReason reason, TracerView<ActionTrace> tracer);
    }
}
