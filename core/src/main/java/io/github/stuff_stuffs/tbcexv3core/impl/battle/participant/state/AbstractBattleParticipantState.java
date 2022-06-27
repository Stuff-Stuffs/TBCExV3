package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.state;

import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;

public interface AbstractBattleParticipantState extends BattleParticipantState {
    void setup(BattleState state);
}
