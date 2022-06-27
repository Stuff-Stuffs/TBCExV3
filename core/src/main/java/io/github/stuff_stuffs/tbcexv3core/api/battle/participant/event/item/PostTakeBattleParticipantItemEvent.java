package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.item;

import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantStateView;

public interface PostTakeBattleParticipantItemEvent {
    void postTakeItem(BattleParticipantItemStack stack, BattleParticipantState state);

    interface View {
        void postTakeItem(BattleParticipantItemStack stack, BattleParticipantStateView state);
    }

    static PostTakeBattleParticipantItemEvent convert(final View view) {
        return view::postTakeItem;
    }

    static PostTakeBattleParticipantItemEvent invoker(final PostTakeBattleParticipantItemEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (stack, state) -> {
            enter.run();
            for (final PostTakeBattleParticipantItemEvent listener : listeners) {
                listener.postTakeItem(stack, state);
            }
            exit.run();
        };
    }
}
