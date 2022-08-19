package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface PostBattleBoundsSet {
    void postBattleBoundsSet(BattleState state, BattleBounds oldBounds, Tracer<ActionTrace> tracer);

    interface View {
        void postBattleBoundsSet(BattleStateView view, BattleBounds oldBounds, Tracer<ActionTrace> tracer);
    }

    static PostBattleBoundsSet convert(final PostBattleBoundsSet.View view) {
        return view::postBattleBoundsSet;
    }

    static PostBattleBoundsSet invoker(final PostBattleBoundsSet[] listeners, final Runnable enter, final Runnable exit) {
        return (state, oldBounds, tracer) -> {
            enter.run();
            for (final PostBattleBoundsSet listener : listeners) {
                listener.postBattleBoundsSet(state, oldBounds, tracer);
            }
            exit.run();
        };
    }
}
