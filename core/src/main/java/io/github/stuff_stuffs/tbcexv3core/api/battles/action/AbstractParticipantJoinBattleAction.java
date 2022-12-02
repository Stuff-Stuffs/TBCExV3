package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;

public interface AbstractParticipantJoinBattleAction extends BattleAction {
    BattleParticipantStateBuilder.Built built();
}
