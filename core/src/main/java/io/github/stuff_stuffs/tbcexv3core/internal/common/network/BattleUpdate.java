package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;

import java.util.List;

public record BattleUpdate(BattleHandle handle, List<BattleAction> actions, int offset) {
    public static final Codec<BattleUpdate> CODEC = RecordCodecBuilder.create(instance -> instance.group(BattleHandle.codec().fieldOf("handle").forGetter(BattleUpdate::handle), Codec.list(BattleAction.NETWORK_CODEC).fieldOf("actions").forGetter(BattleUpdate::actions), Codec.INT.fieldOf("offset").forGetter(BattleUpdate::offset)).apply(instance, BattleUpdate::new));
}
