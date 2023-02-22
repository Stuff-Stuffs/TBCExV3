package io.github.stuff_stuffs.tbcexv3model.internal.common;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TBCExV3Model implements ModInitializer {
    public static final String MOD_ID = "tbcexv3model";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
    }

    public static Identifier id(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
