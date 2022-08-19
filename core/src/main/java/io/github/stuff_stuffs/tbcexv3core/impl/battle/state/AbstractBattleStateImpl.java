package io.github.stuff_stuffs.tbcexv3core.impl.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;

public interface AbstractBattleStateImpl extends BattleState {
    void setup(BattleHandle handle);
}
