package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;

public interface AbstractBattleParticipantInventoryHandle extends BattleParticipantInventoryHandle {
    long getKey();

    static AbstractBattleParticipantInventoryHandle of(final BattleParticipantHandle handle, final long key) {
        return new BattleParticipantInventoryHandleImpl(handle, key);
    }

    Codec<AbstractBattleParticipantInventoryHandle> CODEC = RecordCodecBuilder.create(instance -> instance.group(BattleParticipantHandle.codec().fieldOf("parent").forGetter(BattleParticipantInventoryHandle::getParentHandle), Codec.LONG.fieldOf("key").forGetter(AbstractBattleParticipantInventoryHandle::getKey)).apply(instance, AbstractBattleParticipantInventoryHandle::of));
    Codec<BattleParticipantInventoryHandle> CASTED_CODEC = CodecUtil.castedCodec(CODEC, AbstractBattleParticipantInventoryHandle.class, BattleParticipantInventoryHandle.class);
}
