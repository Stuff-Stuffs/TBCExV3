package io.github.stuff_stuffs.tbcexv3core.impl.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import net.minecraft.util.registry.RegistryEntry;

import java.util.function.BinaryOperator;

public class BattleEntityComponentTypeImpl<T extends BattleEntityComponent> implements BattleEntityComponentType<T> {
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;
    private final BinaryOperator<T> combiner;
    private final RegistryEntry.Reference<BattleEntityComponentType<?>> reference;

    public BattleEntityComponentTypeImpl(final Encoder<T> encoder, final Decoder<T> decoder, final BinaryOperator<T> combiner) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.combiner = combiner;
        this.reference = BattleEntityComponentType.REGISTRY.createEntry(this);
    }

    @Override
    public <K> DataResult<T> decode(final DynamicOps<K> ops, final K encoded) {
        return decoder.parse(ops, encoded);
    }

    @Override
    public <K> DataResult<K> encode(final DynamicOps<K> ops, final BattleEntityComponent component) {
        if (component.getType() != this) {
            throw new TBCExException("Type mismatch!");
        }
        return encoder.encodeStart(ops, (T) component);
    }

    @Override
    public T combine(final T first, final T second) {
        return combiner.apply(first, second);
    }

    @Override
    public RegistryEntry.Reference<BattleEntityComponentType<?>> getReference() {
        return reference;
    }
}
