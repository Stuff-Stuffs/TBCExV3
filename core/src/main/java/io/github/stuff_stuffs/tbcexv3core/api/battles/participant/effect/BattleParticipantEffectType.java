package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect;

import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.effect.BattleParticipantEffectTypeImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BinaryOperator;

@ApiStatus.NonExtendable
public interface BattleParticipantEffectType<View extends BattleParticipantEffect, Effect extends View> {
    Registry<BattleParticipantEffectType<?, ?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleParticipantEffectType<?, ?>>ofRegistry(TBCExV3Core.createId("battle_participant_effects")), Lifecycle.stable(), false)).buildAndRegister();
    Codec<BattleParticipantEffectType<?, ?>> CODEC = REGISTRY.getCodec();

    <K> DataResult<Effect> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleParticipantEffect action);

    RegistryEntry.Reference<BattleParticipantEffectType<?, ?>> getReference();

    Class<View> getViewClass();

    Class<Effect> getEffectClass();

    Effect combine(Effect first, Effect second);

    default @Nullable Effect checkedCast(final BattleParticipantEffect effect) {
        if (effect.getType() == this) {
            return (Effect) effect;
        }
        return null;
    }

    default <V extends BattleParticipantEffect, E extends V> @Nullable BattleParticipantEffectType<V, E> checkedTypeCast(final Class<V> viewClass, final Class<E> effectClass) {
        if (viewClass == getViewClass()) {
            final BattleParticipantEffectType<V, ?> partialCast = (BattleParticipantEffectType<V, ?>) this;
            if (effectClass == partialCast.getEffectClass()) {
                return (BattleParticipantEffectType<V, E>) partialCast;
            }
        }
        return null;
    }

    static <View extends BattleParticipantEffect, Effect extends View> BattleParticipantEffectType<View, Effect> create(final Class<View> viewClass, final Class<Effect> effectClass, final BinaryOperator<Effect> combiner, final Codec<Effect> codec) {
        return create(viewClass, effectClass, combiner, codec, codec);
    }

    static <View extends BattleParticipantEffect, Effect extends View> BattleParticipantEffectType<View, Effect> create(final Class<View> viewClass, final Class<Effect> effectClass, final BinaryOperator<Effect> combiner, final Encoder<Effect> encoder, final Decoder<Effect> decoder) {
        return new BattleParticipantEffectTypeImpl<>(viewClass, effectClass, combiner, encoder, decoder);
    }

    static <View extends BattleParticipantEffect, Effect extends View> Optional<BattleParticipantEffectType<View, Effect>> get(final Identifier id, final Class<View> viewClass, final Class<Effect> effectClass) {
        return REGISTRY.getOrEmpty(id).map(action -> action.checkedTypeCast(viewClass, effectClass));
    }
}
