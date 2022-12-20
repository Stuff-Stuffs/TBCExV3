package io.github.stuff_stuffs.tbcexv3core.impl.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.function.BinaryOperator;

public class BattleEntityComponentTypeImpl<T extends BattleEntityComponent> implements BattleEntityComponentType<T> {
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;
    private final Encoder<T> networkEncoder;
    private final Decoder<T> networkDecoder;
    private final BinaryOperator<T> combiner;
    private final Set<Identifier> happenBefore;
    private final Set<Identifier> happensAfter;

    public BattleEntityComponentTypeImpl(final Encoder<T> encoder, final Decoder<T> decoder, final Encoder<T> networkEncoder, final Decoder<T> networkDecoder, final BinaryOperator<T> combiner, final Set<Identifier> happenBefore, final Set<Identifier> happensAfter) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.networkEncoder = networkEncoder;
        this.networkDecoder = networkDecoder;
        this.combiner = combiner;
        this.happenBefore = Set.copyOf(happenBefore);
        this.happensAfter = Set.copyOf(happensAfter);
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
    public <K> DataResult<T> networkDecode(final DynamicOps<K> ops, final K encoded) {
        return networkDecoder.parse(ops, encoded);
    }

    @Override
    public <K> DataResult<K> networkEncode(final DynamicOps<K> ops, final BattleEntityComponent component) {
        if (component.getType() != this) {
            throw new TBCExException("Type mismatch!");
        }
        return networkEncoder.encodeStart(ops, (T) component);
    }

    @Override
    public Set<Identifier> happensBefore() {
        return happenBefore;
    }

    @Override
    public Set<Identifier> happensAfter() {
        return happensAfter;
    }

    @Override
    public T combine(final T first, final T second) {
        return combiner.apply(first, second);
    }
}
