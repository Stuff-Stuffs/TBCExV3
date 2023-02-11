package io.github.stuff_stuffs.tbcexv3core.api.battles.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.util.math.BlockPos;

public interface BattleEnvironmentBlock {
    void deinit(Tracer<ActionTrace> tracer);

    interface Factory {
        BattleEnvironmentBlock create(BattleState state, BlockPos pos, Tracer<ActionTrace> tracer);
    }
}
