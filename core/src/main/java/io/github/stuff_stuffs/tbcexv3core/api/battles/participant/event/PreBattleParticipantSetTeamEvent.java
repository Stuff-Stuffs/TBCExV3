package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PreBattleParticipantSetTeamEvent {
    boolean preSetTeam(BattleParticipantState state, BattleParticipantTeam newTeam, Tracer<ActionTrace> tracer);

    interface View {
        void preSetTeam(BattleParticipantStateView state, BattleParticipantTeam newTeam, TracerView<ActionTrace> tracer);
    }

    static PreBattleParticipantSetTeamEvent convert(final View view) {
        return (state, newTeam, tracer) -> {
            view.preSetTeam(state, newTeam, tracer);
            return true;
        };
    }

    static PreBattleParticipantSetTeamEvent invoker(final PreBattleParticipantSetTeamEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, newTeam, tracer) -> {
            boolean b = true;
            enter.run();
            for (final PreBattleParticipantSetTeamEvent event : events) {
                b &= event.preSetTeam(state, newTeam, tracer);
            }
            exit.run();
            return b;
        };
    }
}
