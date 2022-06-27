package io.github.stuff_stuffs.tbcexv3core.api.battle.effect;

import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.effect.BattleEffectTypeImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@ApiStatus.NonExtendable
public interface BattleEffectType<View extends BattleEffect, Effect extends View> {
    Registry<BattleEffectType<?, ?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleEffectType<?, ?>>ofRegistry(TBCExV3Core.createId("battle_effects")), Lifecycle.stable(), BattleEffectType::getReference)).buildAndRegister();
    Codec<BattleEffectType<?, ?>> CODEC = REGISTRY.getCodec();

    <K> DataResult<Effect> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleEffect action);

    RegistryEntry.Reference<BattleEffectType<?, ?>> getReference();

    Class<View> getViewClass();

    Class<Effect> getEffectClass();

    default <V extends BattleEffect, E extends V> @Nullable BattleEffectType<V, E> checkedCast(final Class<V> viewClass, final Class<E> effectClass) {
        if (viewClass == getViewClass()) {
            final BattleEffectType<V, ?> partialCast = (BattleEffectType<V, ?>) this;
            if (effectClass == partialCast.getEffectClass()) {
                return (BattleEffectType<V, E>) partialCast;
            }
        }
        return null;
    }

    static <View extends BattleEffect, Effect extends View> BattleEffectType<View, Effect> create(final Class<View> viewClass, final Class<Effect> effectClass, final Codec<Effect> codec) {
        return create(viewClass, effectClass, codec, codec);
    }

    static <View extends BattleEffect, Effect extends View> BattleEffectType<View, Effect> create(final Class<View> viewClass, final Class<Effect> effectClass, final Encoder<Effect> encoder, final Decoder<Effect> decoder) {
        return new BattleEffectTypeImpl<>(viewClass, effectClass, encoder, decoder);
    }

    static <View extends BattleEffect, Effect extends View> Optional<BattleEffectType<View, Effect>> get(final Identifier id, final Class<View> viewClass, final Class<Effect> effectClass) {
        return REGISTRY.getOrEmpty(id).map(action -> action.checkedCast(viewClass, effectClass));
    }
}
