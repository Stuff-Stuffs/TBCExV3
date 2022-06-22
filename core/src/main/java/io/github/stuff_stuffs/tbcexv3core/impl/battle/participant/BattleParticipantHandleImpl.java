package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant;

import io.github.stuff_stuffs.tbcexv3core.api.battle.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.BattleParticipantHandle;

import java.util.UUID;

public record BattleParticipantHandleImpl(UUID uuid, BattleHandle parent) implements BattleParticipantHandle {
    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public BattleHandle getParent() {
        return parent;
    }
}
