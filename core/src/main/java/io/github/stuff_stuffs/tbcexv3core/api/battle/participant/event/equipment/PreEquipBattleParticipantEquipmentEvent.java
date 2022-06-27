package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.equipment;

import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantStateView;

public interface PreEquipBattleParticipantEquipmentEvent {
    boolean preEquip(BattleParticipantState state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot);

    interface View {
        void preEquip(BattleParticipantStateView state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot);
    }

    static PreEquipBattleParticipantEquipmentEvent convert(final View view) {
        return (state, handle, slot) -> {
            view.preEquip(state, handle, slot);
            return true;
        };
    }

    static PreEquipBattleParticipantEquipmentEvent invoker(final PreEquipBattleParticipantEquipmentEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, handle, slot) -> {
            boolean b = true;
            enter.run();
            for (final PreEquipBattleParticipantEquipmentEvent listener : listeners) {
                b &= listener.preEquip(state, handle, slot);
            }
            exit.run();
            return b;
        };
    }
}
