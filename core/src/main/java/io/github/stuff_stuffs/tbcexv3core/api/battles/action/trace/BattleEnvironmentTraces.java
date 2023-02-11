package io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public final class BattleEnvironmentTraces {
    public record EnvironmentSetBlockState(
            BlockPos pos,
            BlockState oldState,
            BlockState newState
    ) implements ActionTrace {
    }

    public record EnvironmentSetBattleBlock(BlockPos pos, boolean replaced) implements ActionTrace {
    }

    public record EnvironmentRemoveBattleBlock(BlockPos pos) implements ActionTrace {
    }

    private BattleEnvironmentTraces() {
    }
}
