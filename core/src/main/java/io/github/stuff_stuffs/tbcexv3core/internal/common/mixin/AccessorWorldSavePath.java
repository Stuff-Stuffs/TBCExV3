package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldSavePath.class)
public interface AccessorWorldSavePath {
    @Invoker(value = "<init>")
    static WorldSavePath create(String relativePath) {
        throw new AssertionError();
    }
}
