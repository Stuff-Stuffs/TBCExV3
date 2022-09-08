package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface PreBattleBoundsSetEvent {
    boolean preBattleBoundsSet(BattleState state, BattleBounds newBounds, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleBoundsSet(BattleStateView view, BattleBounds newBounds, Tracer<ActionTrace> tracer);
    }

    static PreBattleBoundsSetEvent convert(final View view) {
        return (state, newBounds, tracer) -> {
            view.preBattleBoundsSet(state, newBounds, tracer);
            return true;
        };
    }

    static PreBattleBoundsSetEvent invoker(final PreBattleBoundsSetEvent[] listeners, final Runnable enter, final Runnable exit) {
        return (state, newBounds, tracer) -> {
            enter.run();
            boolean accepted = true;
            for (final PreBattleBoundsSetEvent listener : listeners) {
                accepted &= listener.preBattleBoundsSet(state, newBounds, tracer);
            }
            exit.run();
            return accepted;
        };
    }
}
