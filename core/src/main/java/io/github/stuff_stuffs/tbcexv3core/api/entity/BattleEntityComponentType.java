package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.impl.entity.BattleEntityComponentTypeImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.function.BinaryOperator;

public interface BattleEntityComponentType<T extends BattleEntityComponent> {
    Registry<BattleEntityComponentType<?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleEntityComponentType<?>>ofRegistry(TBCExV3Core.createId("battle_entity_components")), Lifecycle.stable(), BattleEntityComponentType::getReference)).buildAndRegister();
    Codec<BattleEntityComponentType<?>> CODEC = REGISTRY.getCodec();

    <K> DataResult<T> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleEntityComponent item);

    T combine(T first, T second);

    RegistryEntry.Reference<BattleEntityComponentType<?>> getReference();

    default @Nullable T checkedCast(final BattleEntityComponent component) {
        if (component.getType() == this) {
            return (T) this;
        }
        return null;
    }

    static <T extends BattleEntityComponent> BattleEntityComponentType<T> of(final Codec<T> codec, final BinaryOperator<T> combiner) {
        return of(codec, codec, combiner);
    }

    static <T extends BattleEntityComponent> BattleEntityComponentType<T> of(final Encoder<T> encoder, final Decoder<T> decoder, final BinaryOperator<T> combiner) {
        return new BattleEntityComponentTypeImpl<>(encoder, decoder, combiner);
    }
}
