package io.github.stuff_stuffs.tbcexv3core.api.battles.environment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public interface BattleEnvironmentView {
    default boolean contains(final BattleBounds bounds) {
        final BlockPos min = min();
        final BlockPos max = max();
        return min.getX() <= bounds.minX && bounds.maxX <= max.getX() &&
                min.getY() <= bounds.minY && bounds.maxY <= max.getY() &&
                min.getZ() <= bounds.minZ && bounds.maxZ <= max.getZ();
    }

    BlockPos min();

    BlockPos max();

    BlockState getBlockState(BlockPos pos);

    RegistryEntry<Biome> getBiome(BlockPos pos);

    Optional<BattleEnvironmentBlock> getBattleBlock(BlockPos pos);

    boolean checkForStanding(BattleParticipantBounds bounds, BlockPos pos, boolean onGround);

    BlockView asBlockView();
}
