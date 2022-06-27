package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.equipment;

import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantStateView;

public interface PostEquipBattleParticipantEquipmentEvent {
    void postEquip(BattleParticipantState state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot);

    interface View {
        void postEquip(BattleParticipantStateView state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot);
    }

    static PostEquipBattleParticipantEquipmentEvent convert(final View view) {
        return view::postEquip;
    }

    static PostEquipBattleParticipantEquipmentEvent invoker(final PostEquipBattleParticipantEquipmentEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, handle, slot) -> {
            enter.run();
            for (final PostEquipBattleParticipantEquipmentEvent listener : listeners) {
                listener.postEquip(state, handle, slot);
            }
            exit.run();
        };
    }
}
