package io.github.stuff_stuffs.tbcexv3content.character.api.job;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.stuff_stuffs.tbcexv3content.character.api.CharacterStatContainer;
import io.github.stuff_stuffs.tbcexv3content.character.api.race.CharacterRacialData;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;

import java.util.Optional;

public interface CharacterJobData {
    Codec<CharacterJobData> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<CharacterJobData, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input).flatMap(map -> CharacterJob.CODEC.parse(ops, map.get("type")).flatMap(type -> type.decode(ops, map.get("data")).map(i -> i)).map(data -> Pair.of(data, ops.empty())));
        }

        @Override
        public <T> DataResult<T> encode(final CharacterJobData input, final DynamicOps<T> ops, final T prefix) {
            return ops.mapBuilder().add("type", CharacterJob.CODEC.encodeStart(ops, input.job())).add("data", input.job().encode(ops, input)).build(prefix);
        }
    };

    CharacterJob<?> job();

    CharacterStatContainer stats(CharacterRacialData data);

    Optional<BattleParticipantEffect> jobBattleEffect(CharacterRacialData data);
}
