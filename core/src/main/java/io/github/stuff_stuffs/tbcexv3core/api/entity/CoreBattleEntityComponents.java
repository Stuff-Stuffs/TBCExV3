package io.github.stuff_stuffs.tbcexv3core.api.entity;

import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.util.registry.Registry;

import java.util.Set;

public final class CoreBattleEntityComponents {
    public static final BattleEntityComponentType<DebugBattleEntityComponent> DEBUG_BATTLE_ENTITY_COMPONENT_TYPE = BattleEntityComponentType.of(DebugBattleEntityComponent.CODEC, DebugBattleEntityComponent.COMBINER, Set.of(), Set.of());
    public static final BattleEntityComponentType<PlayerControlledBattleEntityComponent> PLAYER_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE = BattleEntityComponentType.of(PlayerControlledBattleEntityComponent.CODEC, PlayerControlledBattleEntityComponent.COMBINER, Set.of(), Set.of());
    public static final BattleEntityComponentType<AIControlledBattleEntityComponent> AI_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE = BattleEntityComponentType.of(AIControlledBattleEntityComponent.CODEC, AIControlledBattleEntityComponent.COMBINER, Set.of(), Set.of());

    public static void init() {
        Registry.register(BattleEntityComponentType.REGISTRY, TBCExV3Core.createId("debug"), DEBUG_BATTLE_ENTITY_COMPONENT_TYPE);
        Registry.register(BattleEntityComponentType.REGISTRY, TBCExV3Core.createId("player_controlled"), PLAYER_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE);
        Registry.register(BattleEntityComponentType.REGISTRY, TBCExV3Core.createId("ai_controlled"), AI_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE);
    }

    private CoreBattleEntityComponents() {
    }
}
