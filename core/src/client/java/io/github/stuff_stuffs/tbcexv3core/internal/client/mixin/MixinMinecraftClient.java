package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.client.screen.BattleMenuScreen;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {
    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @ModifyArg(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setScreen(Lnet/minecraft/client/gui/screen/Screen;)V"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/InventoryScreen;<init>(Lnet/minecraft/entity/player/PlayerEntity;)V", shift = At.Shift.BEFORE)))
    private Screen modify(final Screen screen) {
        final BattleHandle handle = ((TBCExPlayerEntity) player).tbcex$getCurrentBattle();
        if (handle != null) {
            return new BattleMenuScreen(handle);
        }
        return screen;
    }
}
