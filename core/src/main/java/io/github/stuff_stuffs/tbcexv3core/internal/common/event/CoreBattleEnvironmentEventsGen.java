package io.github.stuff_stuffs.tbcexv3core.internal.common.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.*;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

@EventKeyLocation("io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEnvironmentEvents")
@EventPackageLocation("io.github.stuff_stuffs.tbcexv3core.api.battles.event.events")
public final class CoreBattleEnvironmentEventsGen {
    @SimpleEventInfo(type = EventType.SINGLE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface PreSetBattleBlock {
        boolean preSetBattleBlock(BlockPos pos, BattleState state, Optional<BattleEnvironmentBlock> old, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface SuccessfulSetBattleBlock {
        void successfulSetBattleBlock(BlockPos pos, BattleState state, Optional<BattleEnvironmentBlock> old, Optional<BattleEnvironmentBlock> current, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.SINGLE)
    private interface FailedSetBattleBlock {
        void failedSetBattleBlock(BlockPos pos, BattleState state, Optional<BattleEnvironmentBlock> old, Tracer<ActionTrace> tracer);
    }

    @SimpleEventInfo(type = EventType.PRE_SUCCESS_FAILURE, defaultValue = "true", combiner = "Boolean.logicalAnd")
    private interface SetBlockState {
        boolean setBlockState(BlockPos pos, BattleState state, @EventVarRename(name = "oldBlockState", phase = EventPhase.SUCCESS) BlockState newBlockState, Tracer<ActionTrace> tracer);
    }
}
