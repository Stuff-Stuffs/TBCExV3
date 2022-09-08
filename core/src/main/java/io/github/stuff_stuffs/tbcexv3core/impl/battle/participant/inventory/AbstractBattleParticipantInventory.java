package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventory;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;

public interface AbstractBattleParticipantInventory extends BattleParticipantInventory {
    Codec<AbstractBattleParticipantInventory> CODEC = CodecUtil.castedCodec(BattleParticipantInventoryImpl.CODEC, BattleParticipantInventoryImpl.class);

    void setup(BattleParticipantState state, BattleParticipantHandle handle);

    static AbstractBattleParticipantInventory createBlank() {
        return new BattleParticipantInventoryImpl();
    }
}
