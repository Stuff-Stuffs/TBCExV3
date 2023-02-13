package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import io.github.stuff_stuffs.tbcexv3core.internal.client.world.ClientBattleWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public class MixinWorldClient {
    @Inject(method = "close", at = @At("RETURN"))
    private void onClose(final CallbackInfo ci) {
        if (this instanceof ClientBattleWorld) {
            ((ClientBattleWorld) this).tbcex$close();
        }
    }
}
