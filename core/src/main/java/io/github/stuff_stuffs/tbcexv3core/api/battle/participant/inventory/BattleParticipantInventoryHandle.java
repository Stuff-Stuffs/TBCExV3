package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.AbstractBattleParticipantInventoryHandle;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantInventoryHandle {
    BattleParticipantHandle getParentHandle();

    static Codec<BattleParticipantInventoryHandle> codec() {
        return AbstractBattleParticipantInventoryHandle.CASTED_CODEC;
    }
}
