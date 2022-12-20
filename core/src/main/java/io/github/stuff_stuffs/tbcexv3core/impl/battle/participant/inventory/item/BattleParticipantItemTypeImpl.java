package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.item;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemType;
import net.minecraft.registry.entry.RegistryEntry;

public class BattleParticipantItemTypeImpl<T extends BattleParticipantItem> implements BattleParticipantItemType<T> {
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;
    private final Class<T> itemClass;

    public BattleParticipantItemTypeImpl(final Encoder<T> encoder, final Decoder<T> decoder, final Class<T> itemClass) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.itemClass = itemClass;
    }

    @Override
    public <K> DataResult<T> decode(final DynamicOps<K> ops, final K encoded) {
        return decoder.parse(ops, encoded);
    }

    @Override
    public <K> DataResult<K> encode(final DynamicOps<K> ops, final BattleParticipantItem item) {
        if (item.type() != this) {
            return DataResult.error("Type mismatch");
        }
        return encoder.encode((T) item, ops, ops.empty());
    }


    @Override
    public Class<T> getItemClass() {
        return itemClass;
    }
}
