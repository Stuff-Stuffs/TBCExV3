package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.item;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.RegistryEntry;

import java.util.Optional;

public class BattleParticipantItemTypeImpl<T extends BattleParticipantItem> implements BattleParticipantItemType<T> {
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;
    private final Class<T> itemClass;
    private final Optional<TagKey<BattleParticipantEquipmentSlot>> acceptableSlots;
    private final RegistryEntry.Reference<BattleParticipantItemType<?>> reference;

    public BattleParticipantItemTypeImpl(final Encoder<T> encoder, final Decoder<T> decoder, final Class<T> itemClass, final Optional<TagKey<BattleParticipantEquipmentSlot>> acceptableSlots) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.itemClass = itemClass;
        this.acceptableSlots = acceptableSlots;
        reference = BattleParticipantItemType.REGISTRY.createEntry(this);
    }

    @Override
    public Optional<TagKey<BattleParticipantEquipmentSlot>> getAcceptableSlots() {
        return acceptableSlots;
    }

    @Override
    public <K> DataResult<T> decode(final DynamicOps<K> ops, final K encoded) {
        return decoder.parse(ops, encoded);
    }

    @Override
    public <K> DataResult<K> encode(final DynamicOps<K> ops, final BattleParticipantItem item) {
        if (item.getType() != this) {
            return DataResult.error("Type mismatch");
        }
        return encoder.encode((T) item, ops, ops.empty());
    }

    @Override
    public RegistryEntry.Reference<BattleParticipantItemType<?>> getReference() {
        return reference;
    }

    @Override
    public Class<T> getItemClass() {
        return itemClass;
    }
}
