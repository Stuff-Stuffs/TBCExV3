package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantStatModifierKey {
    boolean isDestroyed();

    void destroy(Tracer<ActionTrace> tracer);
}
