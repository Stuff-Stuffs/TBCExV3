package io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface PreBattleBlockSetEvent {
    boolean preBattleBlockSet(BlockPos pos, BattleState state, Optional<BattleEnvironmentBlock> old, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleBlockSet(BlockPos pos, BattleStateView state, Optional<BattleEnvironmentBlock> old, TracerView<ActionTrace> tracer);
    }

    static PreBattleBlockSetEvent convert(final View view) {
        return (pos, state, old, tracer) -> {
            view.preBattleBlockSet(pos, state, old, tracer);
            return true;
        };
    }

    static PreBattleBlockSetEvent invoker(final PreBattleBlockSetEvent[] events, final Runnable enter, final Runnable exit) {
        return (pos, state, old, tracer) -> {
            boolean b = true;
            enter.run();
            for (final PreBattleBlockSetEvent event : events) {
                b &= event.preBattleBlockSet(pos, state, old, tracer);
            }
            exit.run();
            return b;
        };
    }
}
