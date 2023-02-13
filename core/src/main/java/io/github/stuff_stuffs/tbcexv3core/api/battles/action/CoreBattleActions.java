package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.registry.Registry;

public final class CoreBattleActions {
    public static final BattleActionType<NoopBattleAction> NOOP_BATTLE_ACTION_TYPE = BattleActionType.create(NoopBattleAction.class, Codec.unit(NoopBattleAction.INSTANCE));
    public static final BattleActionType<StartBattleAction> START_BATTLE_ACTION_TYPE = BattleActionType.create(StartBattleAction.class, Codec.unit(StartBattleAction.INSTANCE));
    public static final BattleActionType<InitialParticipantJoinBattleAction> INITIAL_PARTICIPANT_JOIN_ACTION = BattleActionType.create(InitialParticipantJoinBattleAction.class, InitialParticipantJoinBattleAction.CODEC, InitialParticipantJoinBattleAction.NETWORK_CODEC);
    public static final BattleActionType<BattleParticipantLeaveBattleAction> BATTLE_PARTICIPANT_LEAVE_ACTION = BattleActionType.create(BattleParticipantLeaveBattleAction.class, BattleParticipantLeaveBattleAction.CODEC);
    public static final BattleActionType<InitialTeamSetupBattleAction> INITIAL_TEAM_SETUP_ACTION = BattleActionType.create(InitialTeamSetupBattleAction.class, InitialTeamSetupBattleAction.CODEC);
    public static final BattleActionType<InitialBoundsBattleAction> INITIAL_BOUNDS_ACTION = BattleActionType.create(InitialBoundsBattleAction.class, InitialBoundsBattleAction.CODEC);
    public static final BattleActionType<BattleParticipantTeleportBattleAction> BATTLE_PARTICIPANT_TELEPORT_ACTION = BattleActionType.create(BattleParticipantTeleportBattleAction.class, BattleParticipantTeleportBattleAction.CODEC);
    public static final BattleActionType<BattleParticipantMoveBattleAction> BATTLE_PARTICIPANT_MOVE_ACTION = BattleActionType.create(BattleParticipantMoveBattleAction.class, BattleParticipantMoveBattleAction.CODEC);

    public static void init() {
        Registry.register(BattleActionType.REGISTRY, BattleActionType.NOOP_ID, NOOP_BATTLE_ACTION_TYPE);
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("start"), START_BATTLE_ACTION_TYPE);
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("initial_join"), INITIAL_PARTICIPANT_JOIN_ACTION);
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("participant_leave"), BATTLE_PARTICIPANT_LEAVE_ACTION);
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("initial_team_setup"), INITIAL_TEAM_SETUP_ACTION);
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("initial_bounds"), INITIAL_BOUNDS_ACTION);
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("participant_teleport"), BATTLE_PARTICIPANT_TELEPORT_ACTION);
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("participant_move"), BATTLE_PARTICIPANT_MOVE_ACTION);
    }

    private CoreBattleActions() {
    }
}
