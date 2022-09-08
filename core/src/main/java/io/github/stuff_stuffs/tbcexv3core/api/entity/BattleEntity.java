package io.github.stuff_stuffs.tbcexv3core.api.entity;

import java.util.UUID;

public interface BattleEntity {
    UUID getUuid();

    void buildParticipantState(BattleParticipantStateBuilder builder);
}
