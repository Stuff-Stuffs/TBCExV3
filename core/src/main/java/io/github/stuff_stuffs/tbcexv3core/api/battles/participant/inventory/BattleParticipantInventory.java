package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;

import java.util.Optional;

public interface BattleParticipantInventory extends BattleParticipantInventoryView {
    boolean swapStack(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack);

    Optional<BattleParticipantInventoryHandle> give(BattleParticipantItemStack stack);

    Optional<BattleParticipantItemStack> takeStack(BattleParticipantInventoryHandle handle);

    boolean equip(BattleParticipantEquipmentSlot slot, BattleParticipantInventoryHandle handle, boolean swap);

    boolean unequip(BattleParticipantEquipmentSlot slot);
}
