package io.github.stuff_stuffs.tbcexv3core.api.battle.action;

import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public enum NoopBattleAction implements BattleAction {
    INSTANCE;

    @Override
    public BattleActionType<?> getType() {
        return BattleActionType.NOOP_BATTLE_ACTION_TYPE;
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
    }
}
