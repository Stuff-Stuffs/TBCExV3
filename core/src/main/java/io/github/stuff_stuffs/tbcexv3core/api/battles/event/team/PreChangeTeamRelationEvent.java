package io.github.stuff_stuffs.tbcexv3core.api.battles.event.team;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PreChangeTeamRelationEvent {
    boolean preChangeTeamRelation(BattleState state, BattleParticipantTeam first, BattleParticipantTeam second, BattleParticipantTeamRelation oldRelation, BattleParticipantTeamRelation newRelation, Tracer<ActionTrace> tracer);

    interface View {
        void preChangeTeamRelation(BattleStateView state, BattleParticipantTeam first, BattleParticipantTeam second, BattleParticipantTeamRelation oldRelation, BattleParticipantTeamRelation newRelation, TracerView<ActionTrace> tracer);
    }

    static PreChangeTeamRelationEvent convert(final View view) {
        return (state, first, second, oldRelation, newRelation, tracer) -> {
            view.preChangeTeamRelation(state, first, second, oldRelation, newRelation, tracer);
            return true;
        };
    }

    static PreChangeTeamRelationEvent invoker(final PreChangeTeamRelationEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, first, second, oldRelation, newRelation, tracer) -> {
            boolean b = true;
            enter.run();
            for (final PreChangeTeamRelationEvent event : events) {
                b &= event.preChangeTeamRelation(state, first, second, oldRelation, newRelation, tracer);
            }
            exit.run();
            return b;
        };
    }
}
