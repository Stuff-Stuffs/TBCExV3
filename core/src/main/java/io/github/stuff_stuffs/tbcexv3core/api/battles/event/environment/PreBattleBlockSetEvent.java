package io.github.stuff_stuffs.tbcexv3core.api.battles.event.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public interface PreBattleBlockSetEvent {
    boolean preBattleBlockSet(BlockPos pos, BattleState state, Optional<BattleEnvironmentBlock> old, Tracer<ActionTrace> tracer);

    interface View {
        void preBattleBlockSet(BlockPos pos, BattleStateView state, Optional<BattleEnvironmentBlock> old, TracerView<ActionTrace> tracer);
    }
}
