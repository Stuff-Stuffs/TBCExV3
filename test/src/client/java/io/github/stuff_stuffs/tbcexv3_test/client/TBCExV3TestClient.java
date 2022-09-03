package io.github.stuff_stuffs.tbcexv3_test.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class TBCExV3TestClient implements ClientModInitializer {
    public static final String MOD_ID = "tbcexv3_test";
    private static final KeyBinding OPEN_TEST_SCREEN = new KeyBinding(MOD_ID + ".open_test_screen", GLFW.GLFW_KEY_KP_SUBTRACT, "misc");

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(OPEN_TEST_SCREEN);
        ClientTickEvents.START_WORLD_TICK.register(world -> {
            if (OPEN_TEST_SCREEN.wasPressed()) {
            }
        });
    }
}
