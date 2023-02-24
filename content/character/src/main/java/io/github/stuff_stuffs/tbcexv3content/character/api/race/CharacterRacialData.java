package io.github.stuff_stuffs.tbcexv3content.character.api.race;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.stuff_stuffs.tbcexv3content.character.api.CharacterStatContainer;
import io.github.stuff_stuffs.tbcexv3content.character.api.LevelContainer;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;

import java.util.Optional;

public interface CharacterRacialData {
    Codec<CharacterRacialData> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<CharacterRacialData, T>> decode(final DynamicOps<T> ops, final T input) {
            return ops.getMap(input).flatMap(map -> CharacterRace.CODEC.parse(ops, map.get("type")).flatMap(type -> type.decode(ops, map.get("data")).map(i -> i)).map(data -> Pair.of(data, ops.empty())));
        }

        @Override
        public <T> DataResult<T> encode(final CharacterRacialData input, final DynamicOps<T> ops, final T prefix) {
            return ops.mapBuilder().add("type", CharacterRace.CODEC.encodeStart(ops, input.race())).add("data", input.race().encode(ops, input)).build(prefix);
        }
    };

    CharacterRace<?> race();

    CharacterStatContainer stats();

    Optional<BattleParticipantEffect> racialBattleEffect();

    LevelContainer levelContainer();
}
