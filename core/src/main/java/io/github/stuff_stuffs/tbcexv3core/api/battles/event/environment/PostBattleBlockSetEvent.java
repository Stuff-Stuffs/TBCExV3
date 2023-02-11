package io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface PostBattleBlockSetEvent {
    void postBattleBlockSet(BlockPos pos, BattleState state, Optional<BattleEnvironmentBlock> old, Tracer<ActionTrace> tracer);

    interface View {
        void postBattleBlockSet(BlockPos pos, BattleState state, Optional<BattleEnvironmentBlock> old, TracerView<ActionTrace> tracer);
    }

    static PostBattleBlockSetEvent convert(final View view) {
        return view::postBattleBlockSet;
    }

    static PostBattleBlockSetEvent invoker(final PostBattleBlockSetEvent[] events, final Runnable enter, final Runnable exit) {
        return (pos, state, old, tracer) -> {
            enter.run();
            for (final PostBattleBlockSetEvent event : events) {
                event.postBattleBlockSet(pos, state, old, tracer);
            }
            exit.run();
        };
    }
}
