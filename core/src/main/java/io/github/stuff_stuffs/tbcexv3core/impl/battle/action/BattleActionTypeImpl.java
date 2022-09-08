package io.github.stuff_stuffs.tbcexv3core.impl.battle.action;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleActionType;
import net.minecraft.util.registry.RegistryEntry;

public class BattleActionTypeImpl<T extends BattleAction> implements BattleActionType<T> {
    private final Encoder<T> encoder;
    private final Decoder<T> decoder;
    private final Encoder<T> netEncoder;
    private final Decoder<T> netDecoder;
    private final Class<T> actionClass;
    private final RegistryEntry.Reference<BattleActionType<?>> reference;

    public BattleActionTypeImpl(final Encoder<T> encoder, final Decoder<T> decoder, final Encoder<T> netEncoder, final Decoder<T> netDecoder, final Class<T> actionClass) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.netEncoder = netEncoder;
        this.netDecoder = netDecoder;
        this.actionClass = actionClass;
        reference = BattleActionType.REGISTRY.createEntry(this);
    }

    @Override
    public <K> DataResult<T> decode(final DynamicOps<K> ops, final K encoded, final boolean network) {
        final Decoder<T> decoder = network ? netDecoder : this.decoder;
        return decoder.decode(ops, encoded).map(Pair::getFirst);
    }

    @Override
    public <K> DataResult<K> encode(final DynamicOps<K> ops, final BattleAction action, final boolean network) {
        final Encoder<T> encoder = network ? netEncoder : this.encoder;
        if (action.getType() != this) {
            return DataResult.error("Type mismatch");
        }
        return encoder.encode((T) action, ops, ops.empty());
    }

    @Override
    public RegistryEntry.Reference<BattleActionType<?>> getReference() {
        return reference;
    }

    @Override
    public Class<T> getActionClass() {
        return actionClass;
    }
}
