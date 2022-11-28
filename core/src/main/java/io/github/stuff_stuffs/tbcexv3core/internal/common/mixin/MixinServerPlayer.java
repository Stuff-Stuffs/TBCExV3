package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattlePlayerComponentEvent;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.PlayerCurrentBattleSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

//TODO save preBattleGameMode
@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayer extends Entity implements BattleEntity, TBCExPlayerEntity {
    @Shadow
    @Final
    public ServerPlayerInteractionManager interactionManager;
    @Unique
    private @Nullable BattleHandle tbcex$currentBattle;
    @Unique
    private @Nullable GameMode tbcex$preBattleGameMode = null;

    private MixinServerPlayer(final EntityType<?> type, final World world) {
        super(type, world);
    }

    @Override
    public void buildParticipantState(final BattleParticipantStateBuilder builder) {
        BattlePlayerComponentEvent.EVENT.invoker().onStateBuilder((ServerPlayerEntity) (Object) this, builder);
    }

    @Override
    public void tbcex$setCurrentBattle(@Nullable final BattleHandle handle) {
        if (!Objects.equals(handle, tbcex$currentBattle)) {
            if (tbcex$preBattleGameMode == null) {
                tbcex$preBattleGameMode = interactionManager.getGameMode();
            }
            tbcex$currentBattle = handle;
            interactionManager.changeGameMode(GameMode.SPECTATOR);
            PlayerCurrentBattleSender.send((ServerPlayerEntity) (Object) this, handle);
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void tickHook(final CallbackInfo ci) {
        if (tbcex$currentBattle != null) {
            @Nullable final BattleView view = ((BattleWorld) world).tryGetBattleView(tbcex$currentBattle);
            if (view == null || view.getState().getPhase() == BattleStatePhase.FINISHED) {
                tbcex$setCurrentBattle(null);
            }
        }
        if (tbcex$currentBattle == null && tbcex$preBattleGameMode != null) {
            interactionManager.changeGameMode(tbcex$preBattleGameMode);
        }
    }

    @Override
    public @Nullable BattleHandle tbcex$getCurrentBattle() {
        return tbcex$currentBattle;
    }
}
