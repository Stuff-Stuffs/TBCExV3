package io.github.stuff_stuffs.tbcexv3core.api.battles.participant;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.BattleParticipantHandleImpl;

import java.util.UUID;

public interface BattleParticipantHandle {
    UUID getUuid();

    BattleHandle getParent();

    static BattleParticipantHandle of(final UUID uuid, final BattleHandle parent) {
        return new BattleParticipantHandleImpl(uuid, parent);
    }

    static Codec<BattleParticipantHandle> codec() {
        return BattleParticipantHandleImpl.CODEC;
    }
}
