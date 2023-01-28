package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PreEquipBattleParticipantEquipmentEvent {
    boolean preEquip(BattleParticipantState state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot, Tracer<ActionTrace> tracer);

    interface View {
        void preEquip(BattleParticipantStateView state, BattleParticipantInventoryHandle handle, BattleParticipantEquipmentSlot slot, TracerView<ActionTrace> tracer);
    }

    static PreEquipBattleParticipantEquipmentEvent convert(final View view) {
        return (state, handle, slot, tracer) -> {
            view.preEquip(state, handle, slot, tracer);
            return true;
        };
    }

    static PreEquipBattleParticipantEquipmentEvent invoker(final PreEquipBattleParticipantEquipmentEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, handle, slot, tracer) -> {
            boolean b = true;
            enter.run();
            for (final PreEquipBattleParticipantEquipmentEvent listener : listeners) {
                b &= listener.preEquip(state, handle, slot, tracer);
            }
            exit.run();
            return b;
        };
    }
}
