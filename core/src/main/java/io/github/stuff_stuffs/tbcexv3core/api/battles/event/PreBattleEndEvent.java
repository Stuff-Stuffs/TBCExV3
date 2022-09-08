package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface PreBattleEndEvent {
    void preBattleEnd(BattleState state, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleEnd(BattleStateView state, Tracer<ActionTrace> tracer);
    }

    static PreBattleEndEvent convert(final View view) {
        return view::preBattleEnd;
    }

    static PreBattleEndEvent invoker(final PreBattleEndEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, tracer) -> {
            enter.run();
            for (final PreBattleEndEvent event : events) {
                event.preBattleEnd(state, tracer);
            }
            exit.run();
        };
    }
}
