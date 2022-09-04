package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public interface BattleEntityComponentType<T extends BattleEntityComponent> {
    <K> DataResult<T> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleEntityComponent item);
}
