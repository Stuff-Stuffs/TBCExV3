package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;

public interface BattleAccess extends BattleView {
    boolean tryPushAction(BattleAction action);
}
