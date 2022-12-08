package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;

public final class CoreBattleActionTargetTypes {
    public static final BattleParticipantActionTargetType<BattleParticipantActionBattleParticipantTarget> BATTLE_PARTICIPANT_TARGET_TYPE = BattleParticipantActionTargetType.of(TBCExV3Core.createId("participant"), BattleParticipantActionBattleParticipantTarget.class);
    public static final BattleParticipantActionTargetType<BattleParticipantActionBlockPosTarget> BLOCK_POS_TARGET_TYPE = BattleParticipantActionTargetType.of(TBCExV3Core.createId("block_pos"), BattleParticipantActionBlockPosTarget.class);

    private CoreBattleActionTargetTypes() {
    }
}
