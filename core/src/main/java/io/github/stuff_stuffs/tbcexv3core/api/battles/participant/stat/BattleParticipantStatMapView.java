package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import io.github.stuff_stuffs.tbcexv3util.api.util.OperationChainDisplayBuilder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface BattleParticipantStatMapView {
    double compute(BattleParticipantStat stat, @Nullable OperationChainDisplayBuilder displayBuilder);
}
