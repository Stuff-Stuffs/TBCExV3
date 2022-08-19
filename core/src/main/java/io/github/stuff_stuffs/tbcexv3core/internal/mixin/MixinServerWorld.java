package io.github.stuff_stuffs.tbcexv3core.internal.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleAccess;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWorld.class)
public class MixinServerWorld implements BattleWorld {
    @Override
    public @Nullable BattleAccess tryGetBattleAccess(final BattleHandle handle) {
        return null;
    }
}
