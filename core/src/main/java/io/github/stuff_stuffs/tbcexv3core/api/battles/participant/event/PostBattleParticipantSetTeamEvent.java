package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PostBattleParticipantSetTeamEvent {
    void postSetTeam(BattleParticipantState state, BattleParticipantTeam oldTeam, Tracer<ActionTrace> tracer);

    interface View {
        void postSetTeam(BattleParticipantStateView state, BattleParticipantTeam oldTeam, TracerView<ActionTrace> tracer);
    }

    static PostBattleParticipantSetTeamEvent convert(final View view) {
        return view::postSetTeam;
    }

    static PostBattleParticipantSetTeamEvent invoker(final PostBattleParticipantSetTeamEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, oldTeam, tracer) -> {
            enter.run();
            for (final PostBattleParticipantSetTeamEvent event : events) {
                event.postSetTeam(state, oldTeam, tracer);
            }
            exit.run();
        };
    }
}
