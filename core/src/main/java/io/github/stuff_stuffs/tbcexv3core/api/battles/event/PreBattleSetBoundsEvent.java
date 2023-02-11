package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PreBattleSetBoundsEvent {
    boolean preBattleSetBounds(BattleState state, BattleBounds newBounds, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleSetBounds(BattleStateView view, BattleBounds newBounds, TracerView<ActionTrace> tracer);
    }

    static PreBattleSetBoundsEvent convert(final View view) {
        return (state, newBounds, tracer) -> {
            view.preBattleSetBounds(state, newBounds, tracer);
            return true;
        };
    }

    static PreBattleSetBoundsEvent invoker(final PreBattleSetBoundsEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, newBounds, tracer) -> {
            enter.run();
            boolean accepted = true;
            for (final PreBattleSetBoundsEvent listener : listeners) {
                accepted &= listener.preBattleSetBounds(state, newBounds, tracer);
            }
            exit.run();
            return accepted;
        };
    }
}
