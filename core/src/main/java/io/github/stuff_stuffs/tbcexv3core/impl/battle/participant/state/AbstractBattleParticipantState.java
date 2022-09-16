package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.AbstractBattleStateImpl;

public interface AbstractBattleParticipantState extends BattleParticipantState {
    void setup(AbstractBattleStateImpl state);
}
