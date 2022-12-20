package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.action.BattleActionTypeImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.NonExtendable
public interface BattleActionType<T extends BattleAction> {
    Identifier NOOP_ID = TBCExV3Core.createId("default");
    Registry<BattleActionType<?>> REGISTRY = FabricRegistryBuilder.from(new SimpleDefaultedRegistry<>(NOOP_ID.toString(), RegistryKey.<BattleActionType<?>>ofRegistry(TBCExV3Core.createId("battle_actions")), Lifecycle.stable(), false)).buildAndRegister();
    Codec<BattleActionType<?>> CODEC = REGISTRY.getCodec();

    <K> DataResult<T> decode(DynamicOps<K> ops, K encoded, boolean network);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleAction action, boolean network);

    RegistryEntry.Reference<BattleActionType<?>> getReference();

    Class<T> getActionClass();

    default <T1 extends BattleAction> BattleActionType<T1> checkedCast(final Class<T1> actionClass) {
        if (actionClass == getActionClass()) {
            return (BattleActionType<T1>) this;
        }
        return null;
    }

    static <T extends BattleAction> BattleActionType<T> create(final Class<T> actionClass, final Codec<T> codec) {
        return create(actionClass, codec, codec);
    }

    static <T extends BattleAction> BattleActionType<T> create(final Class<T> actionClass, final Codec<T> codec, final Codec<T> netCodec) {
        return create(actionClass, codec, codec, netCodec, netCodec);
    }

    static <T extends BattleAction> BattleActionType<T> create(final Class<T> actionClass, final Encoder<T> encoder, final Decoder<T> decoder, final Encoder<T> netEncoder, final Decoder<T> netDecoder) {
        return new BattleActionTypeImpl<>(encoder, decoder, netEncoder, netDecoder, actionClass);
    }

    static <T extends BattleAction> Optional<BattleActionType<T>> get(final Identifier id, final Class<T> actionClass) {
        return REGISTRY.getOrEmpty(id).map(action -> action.checkedCast(actionClass));
    }
}
