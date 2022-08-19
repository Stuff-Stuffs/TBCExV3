package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

public interface PostGiveBattleParticipantItemEvent {
    void postGiveItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantState state);

    interface View {
        void postGiveItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantStateView state);
    }

    static PostGiveBattleParticipantItemEvent convert(final View view) {
        return view::postGiveItem;
    }

    static PostGiveBattleParticipantItemEvent invoker(final PostGiveBattleParticipantItemEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (handle, stack, state) -> {
            enter.run();
            for (final PostGiveBattleParticipantItemEvent listener : listeners) {
                listener.postGiveItem(handle, stack, state);
            }
            exit.run();
        };
    }
}
