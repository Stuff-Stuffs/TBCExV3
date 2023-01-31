package io.github.stuff_stuffs.tbcexv3core.internal.client.mixin;

import net.minecraft.client.font.TextHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextHandler.class)
public interface AccessorTextHandler {
    @Accessor(value = "widthRetriever")
    TextHandler.WidthRetriever tbcex$getWidthRetriever();
}
