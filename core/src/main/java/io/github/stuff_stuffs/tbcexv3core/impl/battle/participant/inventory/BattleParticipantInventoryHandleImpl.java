package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory;

import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.BattleParticipantHandle;

public record BattleParticipantInventoryHandleImpl(
        BattleParticipantHandle handle, long key
) implements AbstractBattleParticipantInventoryHandle {
    @Override
    public BattleParticipantHandle getParentHandle() {
        return handle;
    }

    @Override
    public long getKey() {
        return key;
    }
}
