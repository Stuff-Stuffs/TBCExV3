package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattlePlayerComponentEvent;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayer implements BattleEntity {
    @Override
    public void buildParticipantState(final BattleParticipantStateBuilder builder) {
        BattlePlayerComponentEvent.EVENT.invoker().onStateBuilder(builder);
    }
}
