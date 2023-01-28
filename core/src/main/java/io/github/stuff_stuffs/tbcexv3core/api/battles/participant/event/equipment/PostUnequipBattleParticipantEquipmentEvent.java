package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PostUnequipBattleParticipantEquipmentEvent {
    void postUnequip(BattleParticipantState state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot, BattleParticipantItemStack stack, Tracer<ActionTrace> tracer);

    interface View {
        void postUnequip(BattleParticipantStateView state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot, BattleParticipantItemStack stack, TracerView<ActionTrace> tracer);
    }

    static PostUnequipBattleParticipantEquipmentEvent convert(final View view) {
        return view::postUnequip;
    }

    static PostUnequipBattleParticipantEquipmentEvent invoker(final PostUnequipBattleParticipantEquipmentEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, handle, slot, stack, tracer) -> {
            enter.run();
            for (final PostUnequipBattleParticipantEquipmentEvent listener : listeners) {
                listener.postUnequip(state, handle, slot, stack, tracer);
            }
            exit.run();
        };
    }
}
