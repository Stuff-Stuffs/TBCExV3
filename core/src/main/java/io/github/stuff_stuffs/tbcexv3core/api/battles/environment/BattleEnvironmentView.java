package io.github.stuff_stuffs.tbcexv3core.api.battles.environment;

import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public interface BattleEnvironmentView {
    BlockPos min();

    BlockPos max();

    BlockState getBlockState(BlockPos pos);

    RegistryEntry<Biome> getBiome(BlockPos pos);

    Optional<BattleEnvironmentBlock> getBattleBlock(BlockPos pos);
}
