package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PreBattleParticipantSetTeamEvent {
    boolean preSetTeam(BattleParticipantState state, BattleParticipantTeam newTeam, Tracer<ActionTrace> tracer);

    interface View {
        void preSetTeam(BattleParticipantStateView state, BattleParticipantTeam newTeam, TracerView<ActionTrace> tracer);
    }
}
