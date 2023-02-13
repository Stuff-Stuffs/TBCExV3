package io.github.stuff_stuffs.tbcexv3test.common.entity;

import io.github.stuff_stuffs.tbcexv3test.common.CreativeModeBattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3test.common.TBCExV3Test;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.Set;
import java.util.function.BinaryOperator;

public final class TestEntities {
    public static final BattleParticipantEffectType<BattleParticipantEffect, CreativeModeBattleParticipantEffect> CREATIVE_MODE_EFFECT = BattleParticipantEffectType.create(BattleParticipantEffect.class, CreativeModeBattleParticipantEffect.class, new BinaryOperator<CreativeModeBattleParticipantEffect>() {
        @Override
        public CreativeModeBattleParticipantEffect apply(final CreativeModeBattleParticipantEffect effect, final CreativeModeBattleParticipantEffect effect2) {
            return effect;
        }
    }, CreativeModeBattleParticipantEffect.CODEC);
    public static final EntityType<TestEntity> TEST_ENTITY_TYPE = FabricEntityTypeBuilder.createLiving().entityFactory(TestEntity::new).defaultAttributes(LivingEntity::createLivingAttributes).dimensions(EntityDimensions.fixed(1, 1)).build();
    public static final BattleEntityComponentType<TestEntityComponent> TEST_ENTITY_COMPONENT_TYPE = BattleEntityComponentType.of(TestEntityComponent.CODEC, TestEntityComponent.CODEC, TestEntityComponent.COMBINER, Set.of(TBCExV3Core.createId("inventory")), Set.of());

    public static void init() {
        Registry.register(BattleParticipantEffectType.REGISTRY, TBCExV3Test.id("creative"), CREATIVE_MODE_EFFECT);
        Registry.register(Registries.ENTITY_TYPE, TBCExV3Core.createId("test"), TEST_ENTITY_TYPE);
        Registry.register(BattleEntityComponentType.REGISTRY, TBCExV3Core.createId("test"), TEST_ENTITY_COMPONENT_TYPE);
    }

    private TestEntities() {
    }
}
