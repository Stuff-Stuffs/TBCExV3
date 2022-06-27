package io.github.stuff_stuffs.tbcexv3core.impl.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battle.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;

public interface AbstractBattleStateImpl extends BattleState {
    void setup(BattleHandle handle);
}
