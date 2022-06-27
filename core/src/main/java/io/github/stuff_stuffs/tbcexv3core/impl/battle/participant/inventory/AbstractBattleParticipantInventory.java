package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.BattleParticipantInventory;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantState;

public interface AbstractBattleParticipantInventory extends BattleParticipantInventory {
    Codec<AbstractBattleParticipantInventory> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<AbstractBattleParticipantInventory, T>> decode(final DynamicOps<T> ops, final T input) {
            return BattleParticipantInventoryImpl.CODEC.decode(ops, input).flatMap(result -> DataResult.success(Pair.of(result.getFirst(), result.getSecond())));
        }

        @Override
        public <T> DataResult<T> encode(final AbstractBattleParticipantInventory input, final DynamicOps<T> ops, final T prefix) {
            if (input instanceof BattleParticipantInventoryImpl inventory) {
                return BattleParticipantInventoryImpl.CODEC.encode(inventory, ops, prefix);
            }
            return DataResult.error("Did somebody ignore the @ApiStatus.NonExtendable annotation?");
        }
    };

    void setup(BattleParticipantState state, BattleParticipantHandle handle);

    static AbstractBattleParticipantInventory createBlank() {
        return new BattleParticipantInventoryImpl();
    }
}
