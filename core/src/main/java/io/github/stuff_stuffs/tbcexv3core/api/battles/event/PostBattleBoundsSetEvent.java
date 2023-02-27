package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;

public interface PostBattleBoundsSetEvent {
    void postBattleBoundsSet(BattleState state, BattleBounds oldBounds, Tracer<ActionTrace> tracer);

    interface View {
        void postBattleBoundsSet(BattleStateView view, BattleBounds oldBounds, TracerView<ActionTrace> tracer);
    }
}
