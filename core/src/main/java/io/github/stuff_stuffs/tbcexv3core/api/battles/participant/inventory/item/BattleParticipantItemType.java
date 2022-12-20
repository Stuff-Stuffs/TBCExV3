package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item;

import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.item.BattleParticipantItemTypeImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface BattleParticipantItemType<T extends BattleParticipantItem> {
    Registry<BattleParticipantItemType<?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleParticipantItemType<?>>ofRegistry(TBCExV3Core.createId("battle_item_types")), Lifecycle.stable(), false)).buildAndRegister();
    Codec<BattleParticipantItemType<?>> CODEC = REGISTRY.getCodec();

    <K> DataResult<T> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleParticipantItem item);

    Class<T> getItemClass();

    default <T1 extends BattleParticipantItem> @Nullable BattleParticipantItemType<T1> checkedCast(final Class<T1> actionClass) {
        if (actionClass == getItemClass()) {
            return (BattleParticipantItemType<T1>) this;
        }
        return null;
    }

    static <T extends BattleParticipantItem> BattleParticipantItemType<T> create(final Codec<T> codec, final Class<T> itemClass) {
        return create(codec, codec, itemClass);
    }

    static <T extends BattleParticipantItem> BattleParticipantItemType<T> create(final Encoder<T> encoder, final Decoder<T> decoder, final Class<T> itemClass) {
        return new BattleParticipantItemTypeImpl<>(encoder, decoder, itemClass);
    }
}
