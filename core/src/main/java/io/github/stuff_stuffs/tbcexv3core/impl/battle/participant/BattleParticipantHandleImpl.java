package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;

import java.util.UUID;

public record BattleParticipantHandleImpl(UUID uuid, BattleHandle parent) implements BattleParticipantHandle {
    public static final Codec<BattleParticipantHandle> CODEC = RecordCodecBuilder.create(instance -> instance.group(CodecUtil.UUID_CODEC.fieldOf("id").forGetter(BattleParticipantHandle::getUuid), BattleHandle.codec().fieldOf("parent").forGetter(BattleParticipantHandle::getParent)).apply(instance, BattleParticipantHandle::of));

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public BattleHandle getParent() {
        return parent;
    }
}
