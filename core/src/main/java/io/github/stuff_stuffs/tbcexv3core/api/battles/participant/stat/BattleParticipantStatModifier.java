package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import io.github.stuff_stuffs.tbcexv3util.api.util.OperationChainDisplayBuilder;
import org.jetbrains.annotations.Nullable;

public interface BattleParticipantStatModifier {
    BattleParticipantStatModifierPhase getPhase();

    double modify(double value, @Nullable OperationChainDisplayBuilder displayBuilder);
}
