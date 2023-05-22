package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.EventViewable;

import java.util.Optional;

@EventViewable(viewClass = BattleParticipantInventoryView.class)
public interface BattleParticipantInventory extends BattleParticipantInventoryView {
    boolean swapStack(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, Tracer<ActionTrace> tracer);

    Optional<BattleParticipantInventoryHandle> give(BattleParticipantItemStack stack, Tracer<ActionTrace> tracer);

    Optional<BattleParticipantItemStack> takeStack(BattleParticipantInventoryHandle handle, Tracer<ActionTrace> tracer);

    boolean equip(BattleParticipantEquipmentSlot slot, BattleParticipantInventoryHandle handle, boolean swap, Tracer<ActionTrace> tracer);

    boolean unequip(BattleParticipantEquipmentSlot slot, Tracer<ActionTrace> tracer);
}
