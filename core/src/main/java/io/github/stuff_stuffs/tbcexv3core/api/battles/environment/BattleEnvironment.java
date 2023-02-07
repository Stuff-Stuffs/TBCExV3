package io.github.stuff_stuffs.tbcexv3core.api.battles.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface BattleEnvironment extends BattleEnvironmentView {
    boolean setBlockState(BlockPos pos, BlockState state, Tracer<ActionTrace> tracer);

    boolean setBattleBlock(BlockPos pos, BattleEnvironmentBlock.Factory factory, Tracer<ActionTrace> tracer);

    boolean removeBattleBlock(BlockPos pos, Tracer<ActionTrace> tracer);
}
