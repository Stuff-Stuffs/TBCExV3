package io.github.stuff_stuffs.tbcexv3core.api.battle;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleStateView;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleView {
    BattleStateView getState();

    int getActionCount();

    BattleAction getAction(int index);
}
