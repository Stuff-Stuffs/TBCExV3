package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;

public record BattleUpdateRequest(BattleHandle handle, int lastKnownGoodState) {
    public static Codec<BattleUpdateRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(BattleHandle.codec().fieldOf("handle").forGetter(BattleUpdateRequest::handle), Codec.INT.fieldOf("lastKnownGoodState").forGetter(BattleUpdateRequest::lastKnownGoodState)).apply(instance, BattleUpdateRequest::new));
}
