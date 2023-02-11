package io.github.stuff_stuffs.tbcexv3core.impl.battles;

import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.environment.BattleEnvironmentSection;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class ClientBattleEnvironmentImpl extends BattleEnvironmentImpl {
    public ClientBattleEnvironmentImpl(final BlockState outOfBoundsState, final RegistryEntry<Biome> outOfBoundsBiome, final BlockPos min, final BlockPos max, final BattleEnvironmentSection[] sections) {
        super(outOfBoundsState, outOfBoundsBiome, min, max, sections);
    }
}
