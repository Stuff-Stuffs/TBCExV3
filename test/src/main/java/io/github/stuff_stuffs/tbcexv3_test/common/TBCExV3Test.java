package io.github.stuff_stuffs.tbcexv3_test.common;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.LoggerFactory;

public class TBCExV3Test implements ModInitializer, PreLaunchEntrypoint {
    @Override
    public void onInitialize() {

    }

    @Override
    public void onPreLaunch() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                System.loadLibrary("renderdoc");
            } catch (final Exception e) {
                LoggerFactory.getLogger(TBCExV3Test.class).error("Render doc not found, rendering debug will be disabled!");
            }
        }
    }
}
