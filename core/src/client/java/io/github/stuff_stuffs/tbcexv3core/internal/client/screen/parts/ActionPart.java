package io.github.stuff_stuffs.tbcexv3core.internal.client.screen.parts;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

import java.util.List;
import java.util.Optional;

public final class ActionPart {
    private ActionPart() {
    }

    public static List<BattleParticipantAction> actions(final BattleParticipantStateView state, final BattleParticipantEquipmentSlot slot) {
        final Optional<BattleParticipantInventoryHandle> handle = state.getInventory().getHandle(slot);
        if (handle.isPresent()) {
            final Optional<BattleParticipantItemStack> stack = state.getInventory().getStack(handle.get());
            if (stack.isPresent()) {
                return stack.get().getItem().actions(state, stack.get(), handle);
            }
        }
        return List.of();
    }
}
