package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;

import java.util.Iterator;
import java.util.Optional;

public interface BattleParticipantInventoryView {
    Optional<BattleParticipantItemStack> getStack(BattleParticipantInventoryHandle handle);

    Optional<BattleParticipantInventoryHandle> getHandle(BattleParticipantEquipmentSlot slot);

    Optional<BattleParticipantEquipmentSlot> getSlot(BattleParticipantInventoryHandle handle);

    Iterator<BattleParticipantInventoryHandle> getHandles();
}
