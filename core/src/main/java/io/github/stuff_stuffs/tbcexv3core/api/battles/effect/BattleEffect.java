package io.github.stuff_stuffs.tbcexv3core.api.battles.effect;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;

public interface BattleEffect {
    void init(BattleState state, Tracer<ActionTrace> tracer);

    void deinit(Tracer<ActionTrace> tracer);

    BattleEffectType<?, ?> getType();
}
