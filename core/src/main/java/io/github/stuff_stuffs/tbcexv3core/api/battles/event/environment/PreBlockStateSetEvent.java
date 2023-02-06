package io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface PreBlockStateSetEvent {
    boolean preBlockStateSet(BlockPos pos, BattleState state, BlockState newBlockState, Tracer<ActionTrace> tracer);

    interface View {
        void preBlockStateSet(BlockPos pos, BattleStateView state, BlockState newBlockState, TracerView<ActionTrace> tracer);
    }

    static PreBlockStateSetEvent convert(final View view) {
        return (pos, state, newBlockState, tracer) -> {
            view.preBlockStateSet(pos, state, newBlockState, tracer);
            return true;
        };
    }

    static PreBlockStateSetEvent invoker(final PreBlockStateSetEvent[] events, final Runnable enter, final Runnable exit) {
        return (pos, state, newBlockState, tracer) -> {
            boolean b = true;
            enter.run();
            for (final PreBlockStateSetEvent event : events) {
                b &= event.preBlockStateSet(pos, state, newBlockState, tracer);
            }
            exit.run();
            return b;
        };
    }
}
