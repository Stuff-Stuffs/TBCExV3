package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PreGiveBattleParticipantItemEvent {
    boolean preGiveItem(BattleParticipantItemStack stack, BattleParticipantState state, Tracer<ActionTrace> tracer);

    interface View {
        void preGiveItem(BattleParticipantItemStack stack, BattleParticipantStateView state, TracerView<ActionTrace> tracer);
    }

    static PreGiveBattleParticipantItemEvent convert(final View view) {
        return (stack, state, tracer) -> {
            view.preGiveItem(stack, state, tracer);
            return true;
        };
    }

    static PreGiveBattleParticipantItemEvent invoker(final PreGiveBattleParticipantItemEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (stack, state, tracer) -> {
            enter.run();
            boolean b = true;
            for (final PreGiveBattleParticipantItemEvent listener : listeners) {
                b &= listener.preGiveItem(stack, state, tracer);
            }
            exit.run();
            return b;
        };
    }
}
