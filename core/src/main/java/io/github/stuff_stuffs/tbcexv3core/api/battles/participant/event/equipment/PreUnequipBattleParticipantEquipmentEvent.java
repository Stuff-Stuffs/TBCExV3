package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

public interface PreUnequipBattleParticipantEquipmentEvent {
    boolean preUnequip(BattleParticipantState state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot);

    interface View {
        void preUnequip(BattleParticipantStateView state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot);
    }

    static PreUnequipBattleParticipantEquipmentEvent convert(final View view) {
        return (state, handle, slot) -> {
            view.preUnequip(state, handle, slot);
            return true;
        };
    }

    static PreUnequipBattleParticipantEquipmentEvent invoker(final PreUnequipBattleParticipantEquipmentEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, handle, slot) -> {
            boolean b = true;
            enter.run();
            for (final PreUnequipBattleParticipantEquipmentEvent listener : listeners) {
                b &= listener.preUnequip(state, handle, slot);
            }
            exit.run();
            return b;
        };
    }
}
