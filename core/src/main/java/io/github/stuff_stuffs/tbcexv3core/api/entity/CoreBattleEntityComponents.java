package io.github.stuff_stuffs.tbcexv3core.api.entity;

import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.util.registry.Registry;

public final class CoreBattleEntityComponents {
    public static final BattleEntityComponentType<DebugPlayerBattleEntityComponent> DEBUG_PLAYER_BATTLE_ENTITY_COMPONENT_TYPE = BattleEntityComponentType.of(DebugPlayerBattleEntityComponent.CODEC, DebugPlayerBattleEntityComponent.COMBINER);

    public static void init() {
        Registry.register(BattleEntityComponentType.REGISTRY, TBCExV3Core.createId("debug_player"), DEBUG_PLAYER_BATTLE_ENTITY_COMPONENT_TYPE);
    }

    private CoreBattleEntityComponents() {
    }
}
