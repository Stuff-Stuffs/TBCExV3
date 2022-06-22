package io.github.stuff_stuffs.tbcexv3core.internal.common;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class TBCExV3Core implements ModInitializer {
    public static final String MOD_ID = "tbcexv3_core";
    @Override
    public void onInitialize() {

    }

    public static Identifier createId(String path) {
        return new Identifier(MOD_ID, path);
    }
}
