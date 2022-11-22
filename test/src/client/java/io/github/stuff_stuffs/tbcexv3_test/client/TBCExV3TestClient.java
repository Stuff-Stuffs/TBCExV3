package io.github.stuff_stuffs.tbcexv3_test.client;

import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.util.Identifier;

public class TBCExV3TestClient implements ClientModInitializer {
    public static final String MOD_ID = "tbcexv3_test";

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(TestEntities.TEST_ENTITY_TYPE, ctx -> new EntityRenderer<>(ctx) {
            @Override
            public Identifier getTexture(final TestEntity entity) {
                return new Identifier("nop", "nop");
            }
        });
    }
}
