package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface PostBattleEndEvent {
    void postBattleEnd(BattleState state, Tracer<ActionTrace> tracer);

    interface View {
        void postBattleEnd(BattleStateView state, Tracer<ActionTrace> tracer);
    }

    static PostBattleEndEvent convert(final View view) {
        return view::postBattleEnd;
    }

    static PostBattleEndEvent invoker(final PostBattleEndEvent[] events, final Runnable enter, final Runnable exit) {
        return (state, tracer) -> {
            enter.run();
            for (final PostBattleEndEvent event : events) {
                event.postBattleEnd(state, tracer);
            }
            exit.run();
        };
    }
}
