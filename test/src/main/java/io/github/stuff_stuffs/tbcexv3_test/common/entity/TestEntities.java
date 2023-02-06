package io.github.stuff_stuffs.tbcexv3_test.common.entity;

import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.Set;

public final class TestEntities {
    public static final EntityType<TestEntity> TEST_ENTITY_TYPE = FabricEntityTypeBuilder.createLiving().entityFactory(TestEntity::new).defaultAttributes(LivingEntity::createLivingAttributes).dimensions(EntityDimensions.fixed(1, 1)).build();
    public static final BattleEntityComponentType<TestEntityComponent> TEST_ENTITY_COMPONENT_TYPE = BattleEntityComponentType.of(TestEntityComponent.CODEC, TestEntityComponent.CODEC, TestEntityComponent.COMBINER, Set.of(TBCExV3Core.createId("inventory")), Set.of());

    public static void init() {
        Registry.register(Registries.ENTITY_TYPE, TBCExV3Core.createId("test"), TEST_ENTITY_TYPE);
        Registry.register(BattleEntityComponentType.REGISTRY, TBCExV3Core.createId("test"), TEST_ENTITY_COMPONENT_TYPE);
    }

    private TestEntities() {
    }
}
