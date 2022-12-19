package io.github.stuff_stuffs.tbcexv3core.api.entity;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;

import java.util.UUID;

public interface BattleEntity {
    UUID getUuid();

    BattleParticipantBounds getDefaultBounds();

    void buildParticipantState(BattleParticipantStateBuilder builder);
}
