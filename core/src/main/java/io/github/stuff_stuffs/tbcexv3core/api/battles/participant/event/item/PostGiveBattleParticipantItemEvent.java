package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PostGiveBattleParticipantItemEvent {
    void postGiveItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantState state, Tracer<ActionTrace> tracer);

    interface View {
        void postGiveItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantStateView state, TracerView<ActionTrace> tracer);
    }

    static PostGiveBattleParticipantItemEvent convert(final View view) {
        return view::postGiveItem;
    }

    static PostGiveBattleParticipantItemEvent invoker(final PostGiveBattleParticipantItemEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (handle, stack, state, tracer) -> {
            enter.run();
            for (final PostGiveBattleParticipantItemEvent listener : listeners) {
                listener.postGiveItem(handle, stack, state, tracer);
            }
            exit.run();
        };
    }
}
