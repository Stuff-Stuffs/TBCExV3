package io.github.stuff_stuffs.tbcexv3_test.client;

import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.util.Identifier;
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
        EntityRendererRegistry.register(TestEntities.TEST_ENTITY_TYPE, ctx -> new EntityRenderer<>(ctx) {
            @Override
            public Identifier getTexture(final TestEntity entity) {
                return new Identifier("nop", "nop");
            }
        });
    }
}
