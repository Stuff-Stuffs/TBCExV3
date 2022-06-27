package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.equipment;

import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantStateView;

public interface PostUnequipBattleParticipantEquipmentEvent {
    void postUnequip(BattleParticipantState state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot);

    interface View {
        void postUnequip(BattleParticipantStateView state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot);
    }

    static PostUnequipBattleParticipantEquipmentEvent convert(final View view) {
        return view::postUnequip;
    }

    static PostUnequipBattleParticipantEquipmentEvent invoker(final PostUnequipBattleParticipantEquipmentEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, handle, slot) -> {
            enter.run();
            for (final PostUnequipBattleParticipantEquipmentEvent listener : listeners) {
                listener.postUnequip(state, handle, slot);
            }
            exit.run();
        };
    }
}
