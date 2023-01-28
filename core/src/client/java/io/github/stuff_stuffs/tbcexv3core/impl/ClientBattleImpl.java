package io.github.stuff_stuffs.tbcexv3core.impl;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;

public class ClientBattleImpl implements BattleView {
    private final Battle battle;
    private int lastKnownGoodState = -1;

    public ClientBattleImpl(final Battle battle) {
        this.battle = battle;
    }

    @Override
    public BattleStateView getState() {
        return battle.getState();
    }

    @Override
    public int getActionCount() {
        return battle.getActionCount();
    }

    @Override
    public BattleAction getAction(final int index) {
        return battle.getAction(index);
    }

    @Override
    public TracerView<ActionTrace> tracer() {
        return battle.tracer();
    }

    public void update(final BattleUpdate update) {
        if (getActionCount() + 1 < update.offset()) {
            throw new IllegalStateException("Battle update advanced past known position!");
        }
        battle.trimActions(update.offset());
        for (final BattleAction action : update.actions()) {
            battle.pushAction(action);
        }
        lastKnownGoodState = update.offset() + update.actions().size() - 1;
    }

    public BattleUpdateRequest createUpdateRequest() {
        return new BattleUpdateRequest(battle.getState().getHandle(), lastKnownGoodState);
    }
}
