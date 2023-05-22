package io.github.stuff_stuffs.tbcexv3core.internal.common.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.*;

@EventKeyLocation("io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents")
@EventPackageLocation("io.github.stuff_stuffs.tbcexv3core.api.battles.event.events")
public final class CoreBattleEventsGen {
    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface BattleBoundsSet {
        boolean battleBoundsSet(BattleState state, @EventVarRename(name = "oldBounds", phase = EventPhase.SUCCESS) BattleBounds newBounds, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface BattleParticipantJoin {
        boolean battleParticipantJoin(BattleParticipantState state, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface PreBattleParticipantLeave {
        boolean preBattleParticipantLeave(BattleParticipantHandle handle, BattleState battleState, BattleParticipantRemovalReason reason, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface FailedBattleParticipantLeave {
        void failedBattleParticipantLeave(BattleParticipantHandle handle, BattleState battleState, BattleParticipantRemovalReason reason, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface SuccessBattleParticipantLeave {
        void successBattleParticipantLeave(BattleParticipantState state, BattleState battleState, BattleParticipantRemovalReason reason, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface BattleEnd {
        void battleEnd(BattleState state, Tracer<ActionTrace> tracer);
    }
}
