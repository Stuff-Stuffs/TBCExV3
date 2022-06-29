package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.item;

import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.item.BattleParticipantItemTypeImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@ApiStatus.NonExtendable
public interface BattleParticipantItemType<T extends BattleParticipantItem> {
    Registry<BattleParticipantItemType<?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleParticipantItemType<?>>ofRegistry(TBCExV3Core.createId("battle_item_types")), Lifecycle.stable(), BattleParticipantItemType::getReference)).buildAndRegister();
    Codec<BattleParticipantItemType<?>> CODEC = REGISTRY.getCodec();

    Optional<TagKey<BattleParticipantEquipmentSlot>> getAcceptableSlots();

    <K> DataResult<T> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleParticipantItem item);

    RegistryEntry.Reference<BattleParticipantItemType<?>> getReference();

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
        return new BattleParticipantItemTypeImpl<>(encoder, decoder, itemClass, Optional.empty());
    }

    static <T extends BattleParticipantItem> BattleParticipantItemType<T> create(final Codec<T> codec, final Class<T> itemClass, final TagKey<BattleParticipantEquipmentSlot> acceptableSlots) {
        return create(codec, codec, itemClass, acceptableSlots);
    }

    static <T extends BattleParticipantItem> BattleParticipantItemType<T> create(final Encoder<T> encoder, final Decoder<T> decoder, final Class<T> itemClass, final TagKey<BattleParticipantEquipmentSlot> acceptableSlots) {
        return new BattleParticipantItemTypeImpl<>(encoder, decoder, itemClass, Optional.of(acceptableSlots));
    }
}
