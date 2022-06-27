package io.github.stuff_stuffs.tbcexv3core.api.battle.event;

import io.github.stuff_stuffs.tbcexv3core.api.battle.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battle.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface PreBattleBoundsSet {
    boolean preBattleBoundsSet(BattleState state, BattleBounds newBounds, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleBoundsSet(BattleStateView view, BattleBounds newBounds, Tracer<ActionTrace> tracer);
    }

    static PreBattleBoundsSet convert(final View view) {
        return (state, newBounds, tracer) -> {
            view.preBattleBoundsSet(state, newBounds, tracer);
            return true;
        };
    }

    static PreBattleBoundsSet invoker(final PreBattleBoundsSet[] listeners, final Runnable enter, final Runnable exit) {
        return (state, newBounds, tracer) -> {
            enter.run();
            boolean accepted = true;
            for (final PreBattleBoundsSet listener : listeners) {
                accepted &= listener.preBattleBoundsSet(state, newBounds, tracer);
            }
            exit.run();
            return accepted;
        };
    }
}
