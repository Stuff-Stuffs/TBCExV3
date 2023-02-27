package io.github.stuff_stuffs.tbcexv3util.api.util;

import net.fabricmc.loader.api.FabricLoader;

public final class TBCExUtil {
    public static final String DEBUG_ARG = "tbcexdebug";
    public static final Boolean DEBUG = FabricLoader.getInstance().isDevelopmentEnvironment() || Boolean.getBoolean(DEBUG_ARG);

    private TBCExUtil() {
    }
}
