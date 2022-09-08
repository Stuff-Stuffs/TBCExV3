package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

public interface BattleEntityComponent {
    Codec<BattleEntityComponent> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<BattleEntityComponent, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input).flatMap(map -> BattleEntityComponentType.CODEC.parse(ops, map.get("type")).flatMap(type -> type.decode(ops, map.get("data")).map(i -> i)).map(action -> Pair.of(action, ops.empty())));
        }

        @Override
        public <T> DataResult<T> encode(final BattleEntityComponent input, final DynamicOps<T> ops, final T prefix) {
            return ops.mapBuilder().add("data", input.getType().encode(ops, input)).add("type", BattleEntityComponentType.CODEC.encodeStart(ops, input.getType())).build(prefix);
        }
    };

    void applyToState(BattleParticipantState state);

    void onLeave(BattleView view, ServerWorld world);

    default void applyToEntityOnJoin(final Entity entity) {
    }

    BattleEntityComponentType<?> getType();
}
