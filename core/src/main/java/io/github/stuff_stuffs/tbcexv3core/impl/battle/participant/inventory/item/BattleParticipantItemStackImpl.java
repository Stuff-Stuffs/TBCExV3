package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.item;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemType;

public record BattleParticipantItemStackImpl(
        BattleParticipantItem item,
        int count
) implements BattleParticipantItemStack {
    public static final Codec<BattleParticipantItemStack> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<BattleParticipantItemStack, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input).flatMap(map ->
                    BattleParticipantItemType.CODEC.parse(ops, map.get("type"))
                            .flatMap(type -> type.decode(ops, map.get("item")))
                            .map(item -> Pair.of(item, Codec.INT.parse(ops, map.get("count"))))
                            .map(pair ->
                                    pair.getSecond()
                                            .result()
                                            .map(integer -> new BattleParticipantItemStackImpl(pair.getFirst(), integer))
                                            .orElseGet(() -> new BattleParticipantItemStackImpl(pair.getFirst(), 1))
                            )
            ).flatMap(result -> DataResult.success(Pair.of(result, ops.empty())));
        }

        @Override
        public <T> DataResult<T> encode(final BattleParticipantItemStack input, final DynamicOps<T> ops, final T prefix) {
            return ops.mapBuilder()
                    .add("item", input.getItem().getType().encode(ops, input.getItem()))
                    .add("type", BattleParticipantItemType.CODEC.encode(input.getItem().getType(), ops, ops.empty()))
                    .add("count", Codec.INT.encode(input.getCount(), ops, ops.empty()))
                    .build(prefix);
        }
    };

    @Override
    public BattleParticipantItem getItem() {
        return item;
    }

    @Override
    public int getCount() {
        return count;
    }
}
