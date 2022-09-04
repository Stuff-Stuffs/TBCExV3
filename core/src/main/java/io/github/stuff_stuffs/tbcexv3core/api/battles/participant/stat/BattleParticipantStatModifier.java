package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface BattleParticipantStatModifier {
    BattleParticipantStatModifierPhase getPhase();

    double modify(double value, Tracer<StatTrace> tracer);
}
