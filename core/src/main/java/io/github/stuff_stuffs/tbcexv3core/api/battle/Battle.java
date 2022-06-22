package io.github.stuff_stuffs.tbcexv3core.api.battle;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface Battle extends BattleView {
    @Override
    BattleState getState();

    void trimActions(int size);

    void pushAction(BattleAction action);
}
