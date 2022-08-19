package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

import java.util.Optional;

public interface BattleAction {
    Codec<BattleAction> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<BattleAction, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input).flatMap(map -> BattleActionType.CODEC.parse(ops, map.get("type")).flatMap(type -> type.decode(ops, map.get("data")).map(i -> i)).map(action -> Pair.of(action, ops.empty())));
        }

        @Override
        public <T> DataResult<T> encode(final BattleAction input, final DynamicOps<T> ops, final T prefix) {
            return ops.mapBuilder().add("data", input.getType().encode(ops, input)).add("type", BattleActionType.CODEC.encodeStart(ops, input.getType())).build(prefix);
        }
    };

    BattleActionType<?> getType();

    Optional<BattleParticipantHandle> getActor();

    void apply(BattleState state, Tracer<ActionTrace> trace);
}
