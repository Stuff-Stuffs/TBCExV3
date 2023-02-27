package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.DiscretePhaseTracker;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.Set;

public interface PreBattleParticipantSetHealthEvent {
    Identifier PHASE_1 = TBCExV3Core.createId("phase_1");
    Identifier PHASE_2 = TBCExV3Core.createId("phase_2");
    Identifier PHASE_3 = TBCExV3Core.createId("phase_3");
    DiscretePhaseTracker PHASE_TRACKER = Util.make(DiscretePhaseTracker.create(), phaseTracker -> {
        phaseTracker.addPhase(PHASE_1, Set.of(), Set.of());
        phaseTracker.addPhase(PHASE_2, Set.of(PHASE_1), Set.of());
        phaseTracker.addPhase(PHASE_3, Set.of(PHASE_2), Set.of());
    });

    double preSetHealth(BattleParticipantState state, double newHealth, Tracer<ActionTrace> tracer);

    Identifier phase();

    interface View {
        void preSetHealth(BattleParticipantStateView state, double newHealth, TracerView<ActionTrace> tracer);

        Identifier phase();
    }
}
