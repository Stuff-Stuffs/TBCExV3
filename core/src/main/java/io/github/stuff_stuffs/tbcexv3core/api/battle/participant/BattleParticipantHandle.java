package io.github.stuff_stuffs.tbcexv3core.api.battle.participant;

import io.github.stuff_stuffs.tbcexv3core.api.battle.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.BattleParticipantHandleImpl;

import java.util.UUID;

public interface BattleParticipantHandle {
    UUID getUUID();

    BattleHandle getParent();

    static BattleParticipantHandle of(final UUID uuid, final BattleHandle parent) {
        return new BattleParticipantHandleImpl(uuid, parent);
    }
}
