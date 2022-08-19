package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

import java.util.Optional;

public enum NoopBattleAction implements BattleAction {
    INSTANCE;

    @Override
    public BattleActionType<?> getType() {
        return CoreBattleActions.NOOP_BATTLE_ACTION_TYPE;
    }

    @Override
    public Optional<BattleParticipantHandle> getActor() {
        return Optional.empty();
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
    }
}
