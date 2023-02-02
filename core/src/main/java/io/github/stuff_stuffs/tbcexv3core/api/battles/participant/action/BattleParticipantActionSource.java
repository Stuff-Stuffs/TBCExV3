package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;

public sealed interface BattleParticipantActionSource {
    record Item(BattleParticipantInventoryHandle handle) implements BattleParticipantActionSource {
    }

    record Equipped(BattleParticipantEquipmentSlot slot) implements BattleParticipantActionSource {
    }

    record Effect(BattleParticipantEffectType<?, ?> effect) implements BattleParticipantActionSource {
    }

    record Default() implements BattleParticipantActionSource {
    }
}
