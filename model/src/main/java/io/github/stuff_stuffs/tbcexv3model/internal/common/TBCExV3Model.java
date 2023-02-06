package io.github.stuff_stuffs.tbcexv3model.internal.common;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class TBCExV3Model implements ModInitializer {
    public static final String MOD_ID = "tbcexv3model";

    @Override
    public void onInitialize() {
    }

    public static Identifier id(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
