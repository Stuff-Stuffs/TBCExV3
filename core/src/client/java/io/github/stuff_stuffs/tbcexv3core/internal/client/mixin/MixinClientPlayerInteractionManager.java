package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Inject(method = "createPlayer(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/stat/StatHandler;Lnet/minecraft/client/recipebook/ClientRecipeBook;ZZ)Lnet/minecraft/client/network/ClientPlayerEntity;", at = @At("RETURN"))
    private void copyCurrentBattle(final ClientWorld world, final StatHandler statHandler, final ClientRecipeBook recipeBook, final boolean lastSneaking, final boolean lastSprinting, final CallbackInfoReturnable<ClientPlayerEntity> cir) {
        final ClientPlayerEntity value = cir.getReturnValue();
        final ClientPlayerEntity old = MinecraftClient.getInstance().player;
        if (old != null) {
            final BattleHandle handle = ((TBCExPlayerEntity) old).tbcex$getCurrentBattle();
            ((TBCExPlayerEntity) value).tbcex$setCurrentBattle(handle);
        }
    }
}
