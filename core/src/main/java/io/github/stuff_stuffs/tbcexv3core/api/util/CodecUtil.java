package io.github.stuff_stuffs.tbcexv3core.api.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public final class CodecUtil {
    public static <K> Codec<K> conversionCodec(final DynamicOps<K> conversionOps) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<K, T>> decode(final DynamicOps<T> ops, final T input) {
                return DataResult.success(Pair.of(ops.convertTo(conversionOps, input), ops.empty()));
            }

            @Override
            public <T> DataResult<T> encode(final K input, final DynamicOps<T> ops, final T prefix) {
                return DataResult.success(conversionOps.convertTo(ops, input));
            }
        };
    }

    public static <S, B extends S> Codec<S> castedCodec(final Codec<B> baseCodec, final Class<B> baseClass, Class<S> derivedClass) {
        return new Codec<>() {
            @Override
            public <T> DataResult<T> encode(final S input, final DynamicOps<T> ops, final T prefix) {
                if (baseClass.isInstance(input)) {
                    return baseCodec.encode((B) input, ops, prefix);
                }
                return DataResult.error("Got " + input.getClass().getSimpleName() + ", expected " + baseClass.getSimpleName() + ", somebody implemented an internal interface!");
            }

            @Override
            public <T> DataResult<Pair<S, T>> decode(final DynamicOps<T> ops, final T input) {
                return baseCodec.decode(ops, input).map(p -> Pair.of(p.getFirst(), p.getSecond()));
            }
        };
    }

    private CodecUtil() {
    }
}
