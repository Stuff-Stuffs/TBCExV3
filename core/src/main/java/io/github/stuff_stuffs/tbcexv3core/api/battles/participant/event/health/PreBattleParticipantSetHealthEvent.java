package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.health;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.DiscretePhaseTracker;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
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

    static PreBattleParticipantSetHealthEvent convert(final View view) {
        return new PreBattleParticipantSetHealthEvent() {
            @Override
            public double preSetHealth(final BattleParticipantState state, final double newHealth, final Tracer<ActionTrace> tracer) {
                view.preSetHealth(state, newHealth, tracer);
                return newHealth;
            }

            @Override
            public Identifier phase() {
                return view.phase();
            }
        };
    }

    static PreBattleParticipantSetHealthEvent invoker(final PreBattleParticipantSetHealthEvent[] events, final Runnable enter, final Runnable exit) {
        return new PreBattleParticipantSetHealthEvent() {
            @Override
            public double preSetHealth(final BattleParticipantState state, double newHealth, final Tracer<ActionTrace> tracer) {
                enter.run();
                for (final PreBattleParticipantSetHealthEvent event : events) {
                    newHealth = event.preSetHealth(state, newHealth, tracer);
                }
                exit.run();
                return newHealth;
            }

            @Override
            public Identifier phase() {
                throw new UnsupportedOperationException("Invoker has no phase!");
            }
        };
    }
}
