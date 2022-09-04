package io.github.stuff_stuffs.tbcexv3core.api.entity;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.OptionalInt;

@ApiStatus.NonExtendable
public interface BattleParticipantStateBuilder {
    void addEffect(BattleParticipantEffect effect);

    void addItem(BattleParticipantItemStack stack, OptionalInt slot);

    boolean tryEquip(BattleParticipantItemStack stack, BattleParticipantEquipmentSlot slot);
}
