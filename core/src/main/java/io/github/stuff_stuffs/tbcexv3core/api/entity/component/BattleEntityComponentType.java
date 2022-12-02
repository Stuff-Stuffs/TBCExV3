package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.impl.entity.BattleEntityComponentTypeImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public interface BattleEntityComponentType<T extends BattleEntityComponent> {
    Registry<BattleEntityComponentType<?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleEntityComponentType<?>>ofRegistry(TBCExV3Core.createId("battle_entity_components")), Lifecycle.stable(), BattleEntityComponentType::getReference)).buildAndRegister();
    Codec<BattleEntityComponentType<?>> CODEC = REGISTRY.getCodec();

    <K> DataResult<T> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleEntityComponent component);

    <K> DataResult<T> networkDecode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> networkEncode(DynamicOps<K> ops, BattleEntityComponent component);

    Set<Identifier> happensBefore();

    Set<Identifier> happensAfter();

    T combine(T first, T second);

    RegistryEntry.Reference<BattleEntityComponentType<?>> getReference();

    default @Nullable T checkedCast(final BattleEntityComponent component) {
        if (component.getType() == this) {
            return (T) this;
        }
        return null;
    }

    static <T extends BattleEntityComponent> BattleEntityComponentType<T> ofClientAbsent(final Codec<T> codec, final Supplier<T> clientDummySupplier, final BinaryOperator<T> combiner, final Set<Identifier> happenBefore, final Set<Identifier> happensAfter) {
        final Codec<T> netCodec = Codec.unit(clientDummySupplier);
        return of(codec, netCodec, combiner, happenBefore, happensAfter);
    }

    static <T extends BattleEntityComponent> BattleEntityComponentType<T> of(final Codec<T> codec, final Codec<T> networkCodec, final BinaryOperator<T> combiner, final Set<Identifier> happenBefore, final Set<Identifier> happensAfter) {
        return of(codec, codec, networkCodec, networkCodec, combiner, happenBefore, happensAfter);
    }

    static <T extends BattleEntityComponent> BattleEntityComponentType<T> of(final Encoder<T> encoder, final Decoder<T> decoder, final Encoder<T> networkEncoder, final Decoder<T> networkDecoder, final BinaryOperator<T> combiner, final Set<Identifier> happenBefore, final Set<Identifier> happensAfter) {
        return new BattleEntityComponentTypeImpl<>(encoder, decoder, networkEncoder, networkDecoder, combiner, happenBefore, happensAfter);
    }
}
