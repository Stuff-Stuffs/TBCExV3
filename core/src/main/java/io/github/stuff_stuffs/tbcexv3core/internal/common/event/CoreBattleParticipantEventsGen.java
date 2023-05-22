package io.github.stuff_stuffs.tbcexv3core.internal.common.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.EventPhaseTracker;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.*;
import net.minecraft.util.Identifier;

import java.util.Comparator;

@EventKeyLocation("io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.CoreBattleParticipantEvents")
@EventPackageLocation("io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.events")
public final class CoreBattleParticipantEventsGen {
    @SimpleEventInfo(type = EventType.SINGLE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface PreGiveBattleParticipantItem {
        boolean preGiveBattleParticipantItem(BattleParticipantItemStack stack, BattleParticipantState state, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface FailedGiveBattleParticipantItem {
        void failedGiveBattleParticipantItem(BattleParticipantItemStack stack, BattleParticipantState state, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface SuccessfulGiveBattleParticipantItem {
        void successfulGiveBattleParticipantItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantState state, Tracer<ActionTrace> tracer);
    }


    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface TakeBattleParticipantItem {
        boolean takeBattleParticipantItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantState state, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface EquipBattleParticipantEquipment {
        boolean equip(BattleParticipantState state, BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantEquipmentSlot slot, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface UnequipBattleParticipantEquipment {
        boolean unequip(BattleParticipantState state, BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantEquipmentSlot slot, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface BattleParticipantSetTeam {
        boolean setTeam(BattleParticipantState state, BattleParticipantTeam team, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface BattleParticipantSetBounds {
        boolean setBounds(BattleParticipantState state, @EventVarRename(name = "oldBounds", phase = EventPhase.SUCCESS) BattleParticipantBounds newBounds, Tracer<ActionTrace> tracer);
    }

    @PassthroughEventInfo(passthrough = "newHealth", type = EventType.SINGLE, combiner = "io.github.stuff_stuffs.tbcexv3core.internal.common.event.CoreBattleParticipantEventsGen.selectSecond", compareBy = "phase", comparator = "io.github.stuff_stuffs.tbcexv3core.internal.common.event.CoreBattleParticipantEventsGen.healthPhaseComparator()")
    private interface PreBattleParticipantSetHealth {
        double preSetHealth(BattleParticipantState state, double newHealth, Tracer<ActionTrace> tracer);

        Identifier phase();
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface SuccessfulBattleParticipantSetHealth {
        void successfulSetHealth(BattleParticipantState state, double oldHealth, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface FailedBattleParticipantSetHealth {
        void failedSetHealth(BattleParticipantState state, double attemptedHealth, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface PreBattleParticipantDeath {
        void preDeath(BattleParticipantState state, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface PostBattleParticipantDeath {
        void postDeath(BattleParticipantState deadState, BattleState battleState, Tracer<ActionTrace> tracer);
    }

    public static double selectSecond(final double a, final double b) {
        return b;
    }

    public static Comparator<Identifier> healthPhaseComparator() {
        return EventPhaseTracker.Health.PHASE_TRACKER.phaseComparator();
    }
}
