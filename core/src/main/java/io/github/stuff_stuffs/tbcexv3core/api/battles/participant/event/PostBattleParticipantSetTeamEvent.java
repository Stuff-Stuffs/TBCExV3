package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PostBattleParticipantSetTeamEvent {
    void postSetTeam(BattleParticipantState state, BattleParticipantTeam oldTeam, Tracer<ActionTrace> tracer);

    interface View {
        void postSetTeam(BattleParticipantStateView state, BattleParticipantTeam oldTeam, TracerView<ActionTrace> tracer);
    }
}
