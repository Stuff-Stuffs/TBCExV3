package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

public interface PreGiveBattleParticipantItemEvent {
    boolean preGiveItem(BattleParticipantItemStack stack, BattleParticipantState state);

    interface View {
        void preGiveItem(BattleParticipantItemStack stack, BattleParticipantStateView state);
    }

    static PreGiveBattleParticipantItemEvent convert(final View view) {
        return (stack, state) -> {
            view.preGiveItem(stack, state);
            return true;
        };
    }

    static PreGiveBattleParticipantItemEvent invoker(final PreGiveBattleParticipantItemEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (stack, state) -> {
            enter.run();
            boolean b = true;
            for (final PreGiveBattleParticipantItemEvent listener : listeners) {
                b &= listener.preGiveItem(stack, state);
            }
            exit.run();
            return b;
        };
    }
}
