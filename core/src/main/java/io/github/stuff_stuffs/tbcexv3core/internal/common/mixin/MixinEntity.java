package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import io.github.stuff_stuffs.tbcexv3core.internal.common.DelayedEntityComponentEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public class MixinEntity implements DelayedEntityComponentEntity {
    @Unique
    private boolean tbcex_delayedComponents;
    @Override
    public boolean tbcex_getDelayedComponentsCheck() {
        return tbcex_delayedComponents;
    }

    @Override
    public void tbcex_setDelayedComponentsCheck(final boolean val) {
        tbcex_delayedComponents = val;
    }
}
