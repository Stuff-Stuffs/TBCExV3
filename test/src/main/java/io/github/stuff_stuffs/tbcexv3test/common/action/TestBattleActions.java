package io.github.stuff_stuffs.tbcexv3test.common.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleActionType;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.registry.Registry;

public final class TestBattleActions {
    public static final BattleActionType<BattleParticipantMeleeTestBattleAction> MELEE_TEST_BATTLE_ACTION = BattleActionType.create(BattleParticipantMeleeTestBattleAction.class, BattleParticipantMeleeTestBattleAction.CODEC);

    public static void init() {
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("melee_test"), MELEE_TEST_BATTLE_ACTION);
    }

    private TestBattleActions() {
    }
}
