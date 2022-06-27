package io.github.stuff_stuffs.tbcexv3core.api.battle.action;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.util.registry.Registry;

public final class CoreBattleActions {
    public static final BattleActionType<NoopBattleAction> NOOP_BATTLE_ACTION_TYPE = BattleActionType.create(NoopBattleAction.class, Codec.unit(NoopBattleAction.INSTANCE));
    public static final BattleActionType<StartBattleAction> START_BATTLE_ACTION_TYPE = BattleActionType.create(StartBattleAction.class, Codec.unit(StartBattleAction.INSTANCE));

    public static void init() {
        Registry.register(BattleActionType.REGISTRY, BattleActionType.NOOP_ID, NOOP_BATTLE_ACTION_TYPE);
        Registry.register(BattleActionType.REGISTRY, TBCExV3Core.createId("start"), START_BATTLE_ACTION_TYPE);
    }

    private CoreBattleActions() {
    }
}
