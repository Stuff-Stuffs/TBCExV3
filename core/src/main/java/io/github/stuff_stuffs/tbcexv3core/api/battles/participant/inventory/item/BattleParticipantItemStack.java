package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.item.BattleParticipantItemStackImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantItemStack {
    BattleParticipantItem getItem();

    int getCount();

    boolean matches(BattleParticipantItemStack other);

    static BattleParticipantItemStack of(final BattleParticipantItem item, final int count) {
        Preconditions.checkArgument(count > 0);
        return new BattleParticipantItemStackImpl(item, count);
    }

    static Codec<BattleParticipantItemStack> codec() {
        return BattleParticipantItemStackImpl.CODEC;
    }
}
