package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battle.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.BattleParticipantHandle;
import net.minecraft.util.dynamic.Codecs;

import java.util.UUID;

public record BattleParticipantHandleImpl(UUID uuid, BattleHandle parent) implements BattleParticipantHandle {
    public static final Codec<BattleParticipantHandle> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.UUID.fieldOf("id").forGetter(BattleParticipantHandle::getUuid), BattleHandle.codec().fieldOf("parent").forGetter(BattleParticipantHandle::getParent)).apply(instance, BattleParticipantHandle::of));

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public BattleHandle getParent() {
        return parent;
    }
}
