package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import org.jetbrains.annotations.Nullable;

public interface BattleParticipantStatModifier {
    BattleParticipantStatModifierPhase getPhase();

    double modify(double value, @Nullable Tracer<StatTrace> tracer);
}
