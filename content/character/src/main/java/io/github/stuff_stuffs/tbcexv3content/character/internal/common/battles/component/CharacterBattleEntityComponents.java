package io.github.stuff_stuffs.tbcexv3content.character.internal.common.battles.component;

import io.github.stuff_stuffs.tbcexv3content.character.internal.common.TBCExV3Character;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.registry.Registry;

import java.util.Set;

public final class CharacterBattleEntityComponents {
    public static final BattleEntityComponentType<CharacterBattleEntityComponent> CHARACTER_BATTLE_ENTITY_COMPONENT_TYPE = BattleEntityComponentType.of(CharacterBattleEntityComponent.CODEC, CharacterBattleEntityComponent.CODEC, CharacterBattleEntityComponent.COMBINER, Set.of(TBCExV3Core.createId("save_entity_data"), TBCExV3Core.createId("player_controlled")), Set.of());

    public static void init() {
        Registry.register(BattleEntityComponentType.REGISTRY, TBCExV3Character.id("character"), CHARACTER_BATTLE_ENTITY_COMPONENT_TYPE);
    }

    private CharacterBattleEntityComponents() {
    }
}
