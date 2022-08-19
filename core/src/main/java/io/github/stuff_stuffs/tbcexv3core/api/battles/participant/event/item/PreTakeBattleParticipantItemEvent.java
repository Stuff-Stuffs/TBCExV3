package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

public interface PreTakeBattleParticipantItemEvent {
    boolean preTakeItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantState state);

    interface View {
        void preTakeItem(BattleParticipantInventoryHandle handle, BattleParticipantItemStack stack, BattleParticipantStateView state);
    }

    static PreTakeBattleParticipantItemEvent convert(final View view) {
        return (handle, stack, state) -> {
            view.preTakeItem(handle, stack, state);
            return true;
        };
    }

    static PreTakeBattleParticipantItemEvent invoker(final PreTakeBattleParticipantItemEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (handle, stack, state) -> {
            enter.run();
            boolean b = true;
            for (final PreTakeBattleParticipantItemEvent listener : listeners) {
                b &= listener.preTakeItem(handle, stack, state);
            }
            exit.run();
            return b;
        };
    }
}
