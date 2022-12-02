package io.github.stuff_stuffs.tbcexv3core.api.battles.event.team;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PostChangeTeamRelationEvent {
    void postChangeTeamRelation(BattleState state, BattleParticipantTeam first, BattleParticipantTeam second, BattleParticipantTeamRelation oldRelation, BattleParticipantTeamRelation newRelation, Tracer<ActionTrace> tracer);

    interface View {
        void postChangeTeamRelation(BattleStateView state, BattleParticipantTeam first, BattleParticipantTeam second, BattleParticipantTeamRelation oldRelation, BattleParticipantTeamRelation newRelation, TracerView<ActionTrace> tracer);
    }

    static PostChangeTeamRelationEvent convert(final View view) {
        return view::postChangeTeamRelation;
    }

    static PostChangeTeamRelationEvent invoker(final PostChangeTeamRelationEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, first, second, oldRelation, newRelation, tracer) -> {
            enter.run();
            for (final PostChangeTeamRelationEvent event : events) {
                event.postChangeTeamRelation(state, first, second, oldRelation, newRelation, tracer);
            }
            exit.run();
        };
    }
}
