package io.github.stuff_stuffs.tbcexv3core.api.battles.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.EventViewable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

//TODO weather, sky and fog
@EventViewable(viewClass = BattleEnvironmentView.class)
public interface BattleEnvironment extends BattleEnvironmentView {
    boolean setBlockState(BlockPos pos, BlockState state, Tracer<ActionTrace> tracer);

    boolean setBattleBlock(BlockPos pos, BattleEnvironmentBlock.Factory factory, Tracer<ActionTrace> tracer);

    boolean removeBattleBlock(BlockPos pos, Tracer<ActionTrace> tracer);
}
