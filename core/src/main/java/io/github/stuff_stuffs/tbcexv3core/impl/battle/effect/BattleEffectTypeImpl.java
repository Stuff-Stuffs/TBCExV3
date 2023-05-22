package io.github.stuff_stuffs.tbcexv3core.impl.battle.effect;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffectType;

public class BattleEffectTypeImpl<View extends BattleEffect, Effect extends View> implements BattleEffectType<View, Effect> {
    private final Class<View> viewClass;
    private final Class<Effect> effectClass;
    private final Encoder<Effect> encoder;
    private final Decoder<Effect> decoder;

    public BattleEffectTypeImpl(final Class<View> viewClass, final Class<Effect> effectClass, final Encoder<Effect> encoder, final Decoder<Effect> decoder) {
        this.viewClass = viewClass;
        this.effectClass = effectClass;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public <K> DataResult<Effect> decode(final DynamicOps<K> ops, final K encoded) {
        return decoder.decode(ops, encoded).map(Pair::getFirst);
    }

    @Override
    public <K> DataResult<K> encode(final DynamicOps<K> ops, final BattleEffect action) {
        if (action.getType() != this) {
            return DataResult.error(() -> "Type mismatch");
        }
        return encoder.encode((Effect) action, ops, ops.empty());
    }

    @Override
    public Class<View> getViewClass() {
        return viewClass;
    }

    @Override
    public Class<Effect> getEffectClass() {
        return effectClass;
    }
}
