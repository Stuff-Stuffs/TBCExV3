package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BattleDisplayWorld {
    RegistryKey<World> BATTLE_DISPLAY_WORLD = RegistryKey.of(RegistryKeys.WORLD, TBCExV3Core.createId("battle_display_world"));

    boolean tbcex$isBattleDisplayWorld();

    BlockPos tbccex$allocate(BattleHandle handle, int maxWidth);

    void tbcex$deallocate(BattleHandle handle);

    void tbcex$apply(BlockPos start, BattleEnvironmentImpl.Initial environment);
}
