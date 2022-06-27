package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.BattleParticipantInventoryHandle;

public interface AbstractBattleParticipantInventoryHandle extends BattleParticipantInventoryHandle {
    long getKey();

    static AbstractBattleParticipantInventoryHandle of(final BattleParticipantHandle handle, final long key) {
        return new BattleParticipantInventoryHandleImpl(handle, key);
    }

    Codec<AbstractBattleParticipantInventoryHandle> CODEC = RecordCodecBuilder.create(instance -> instance.group(BattleParticipantHandle.codec().fieldOf("parent").forGetter(BattleParticipantInventoryHandle::getParentHandle), Codec.LONG.fieldOf("key").forGetter(AbstractBattleParticipantInventoryHandle::getKey)).apply(instance, AbstractBattleParticipantInventoryHandle::of));
    Codec<BattleParticipantInventoryHandle> CASTED_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<BattleParticipantInventoryHandle, T>> decode(final DynamicOps<T> ops, final T input) {
            return CODEC.decode(ops, input).flatMap(result -> DataResult.success(Pair.of(result.getFirst(), result.getSecond())));
        }

        @Override
        public <T> DataResult<T> encode(final BattleParticipantInventoryHandle input, final DynamicOps<T> ops, final T prefix) {
            if (input instanceof AbstractBattleParticipantInventoryHandle handle) {
                return CODEC.encode(handle, ops, prefix);
            }
            return DataResult.error("Did somebody ignore the @ApiStatus.NonExtendable annotation?");
        }
    };
}
