package io.github.stuff_stuffs.tbcexv3core.internal.common.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.EventKeyLocation;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.EventPackageLocation;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.EventType;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.SimpleEventInfo;

@EventKeyLocation("io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleTeamEvents")
@EventPackageLocation("io.github.stuff_stuffs.tbcexv3core.api.battles.event.events")
public final class CoreBattleTeamEventsGen {
    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface ChangeTeamRelation {
        boolean changeTeamRelation(BattleState state, BattleParticipantTeam first, BattleParticipantTeam second, BattleParticipantTeamRelation oldRelation, BattleParticipantTeamRelation newRelation, Tracer<ActionTrace> tracer);
    }
}
