package io.github.stuff_stuffs.tbcexv3core.api.battles.effect;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface BattleEffect {
    void init(BattleState state, Tracer<ActionTrace> tracer);

    void deinit();

    BattleEffectType<?, ?> getType();
}
