package io.github.stuff_stuffs.tbcexv3core.api.battle.effect;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.effect.BattleEffectTypeImpl;

public interface BattleEffect {
    void init(BattleState state, Tracer<ActionTrace> tracer);

    void deinit();

    BattleEffectTypeImpl<?, ?> getType();
}
