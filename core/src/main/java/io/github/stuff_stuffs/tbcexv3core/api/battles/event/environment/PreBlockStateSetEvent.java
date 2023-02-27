package io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface PreBlockStateSetEvent {
    boolean preBlockStateSet(BlockPos pos, BattleState state, BlockState newBlockState, Tracer<ActionTrace> tracer);

    interface View {
        void preBlockStateSet(BlockPos pos, BattleStateView state, BlockState newBlockState, TracerView<ActionTrace> tracer);
    }
}
