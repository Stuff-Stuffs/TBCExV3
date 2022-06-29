package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantStatMapView {
    double compute(BattleParticipantStat stat, Tracer<StatTrace> tracer);
}
