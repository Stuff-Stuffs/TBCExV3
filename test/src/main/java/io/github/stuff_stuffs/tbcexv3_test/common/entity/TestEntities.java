package io.github.stuff_stuffs.tbcexv3_test.common.entity;

import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class TestEntities {
    public static final EntityType<TestEntity> TEST_ENTITY_TYPE = FabricEntityTypeBuilder.createLiving().entityFactory(TestEntity::new).defaultAttributes(LivingEntity::createLivingAttributes).dimensions(EntityDimensions.fixed(1, 1)).build();

    public static void init() {
        Registry.register(Registries.ENTITY_TYPE, TBCExV3Core.createId("test"), TEST_ENTITY_TYPE);
    }

    private TestEntities() {
    }
}
