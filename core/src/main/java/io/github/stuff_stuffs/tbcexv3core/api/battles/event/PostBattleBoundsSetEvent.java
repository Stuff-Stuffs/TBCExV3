package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;

public interface PostBattleBoundsSetEvent {
    void postBattleBoundsSet(BattleState state, BattleBounds oldBounds, Tracer<ActionTrace> tracer);

    interface View {
        void postBattleBoundsSet(BattleStateView view, BattleBounds oldBounds, TracerView<ActionTrace> tracer);
    }

    static PostBattleBoundsSetEvent convert(final PostBattleBoundsSetEvent.View view) {
        return view::postBattleBoundsSet;
    }

    static PostBattleBoundsSetEvent invoker(final PostBattleBoundsSetEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, oldBounds, tracer) -> {
            enter.run();
            for (final PostBattleBoundsSetEvent listener : listeners) {
                listener.postBattleBoundsSet(state, oldBounds, tracer);
            }
            exit.run();
        };
    }
}
