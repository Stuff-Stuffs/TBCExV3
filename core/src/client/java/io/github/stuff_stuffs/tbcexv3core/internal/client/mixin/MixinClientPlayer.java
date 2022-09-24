package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class MixinClientPlayer implements TBCExPlayerEntity {
    @Unique
    private @Nullable BattleHandle tbcex$currentBattle;

    @Override
    public void tbcex$setCurrentBattle(@Nullable final BattleHandle handle) {
        tbcex$currentBattle = handle;
    }

    @Override
    public @Nullable BattleHandle tbcex$getCurrentBattle() {
        return tbcex$currentBattle;
    }
}
