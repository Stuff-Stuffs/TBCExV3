package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item;

import net.minecraft.item.ItemStack;

import java.util.Collection;

public interface BattleParticipantItem {
    BattleParticipantItemType<?> getType();

    Collection<ItemStack> toItemStacks(BattleParticipantItemStack stack);
}
