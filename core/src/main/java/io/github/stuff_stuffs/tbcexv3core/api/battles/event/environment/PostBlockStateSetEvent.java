package io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface PostBlockStateSetEvent {
    void postBlockStateSet(BlockPos pos, BattleState state, BlockState oldBlockState, Tracer<ActionTrace> tracer);

    interface View {
        void postBlockStateSet(BlockPos pos, BattleStateView state, BlockState oldBlockState, TracerView<ActionTrace> tracer);
    }

    static PostBlockStateSetEvent convert(final PostBlockStateSetEvent.View view) {
        return view::postBlockStateSet;
    }

    static PostBlockStateSetEvent invoker(final PostBlockStateSetEvent[] events, final Runnable enter, final Runnable exit) {
        return (pos, state, oldBlockState, tracer) -> {
            enter.run();
            for (final PostBlockStateSetEvent event : events) {
                event.postBlockStateSet(pos, state, oldBlockState, tracer);
            }
            exit.run();
        };
    }
}
