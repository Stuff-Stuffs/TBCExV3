package io.github.stuff_stuffs.tbcexv3core.api.battles.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public interface BattleEnvironment extends BattleEnvironmentView {
    boolean setBiome(BlockPos pos, RegistryEntry<Biome> biome, Tracer<ActionTrace> tracer);

    boolean setBlockState(BlockPos pos, BlockState state, Tracer<ActionTrace> tracer);

    boolean setBattleBlock(BlockPos pos, BattleEnvironmentBlock.Factory factory, Tracer<ActionTrace> tracer);

    boolean removeBattleBlock(BlockPos pos, Tracer<ActionTrace> tracer);
}
