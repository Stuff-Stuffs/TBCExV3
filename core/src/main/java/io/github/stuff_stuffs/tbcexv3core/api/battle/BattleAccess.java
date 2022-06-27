package io.github.stuff_stuffs.tbcexv3core.api.battle;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.BattleAction;

public interface BattleAccess extends BattleView {
    boolean tryPushAction(BattleAction action);
}
