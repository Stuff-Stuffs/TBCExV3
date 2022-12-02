package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PreTakeBattleParticipantItemEvent {
    boolean preTakeItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantState state, Tracer<ActionTrace> tracer);

    interface View {
        void preTakeItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantStateView state, TracerView<ActionTrace> tracer);
    }

    static PreTakeBattleParticipantItemEvent convert(final View view) {
        return (handle, stack, state, tracer) -> {
            view.preTakeItem(handle, stack, state, tracer);
            return true;
        };
    }

    static PreTakeBattleParticipantItemEvent invoker(final PreTakeBattleParticipantItemEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (handle, stack, state, tracer) -> {
            enter.run();
            boolean b = true;
            for (final PreTakeBattleParticipantItemEvent listener : listeners) {
                b &= listener.preTakeItem(handle, stack, state, tracer);
            }
            exit.run();
            return b;
        };
    }
}
