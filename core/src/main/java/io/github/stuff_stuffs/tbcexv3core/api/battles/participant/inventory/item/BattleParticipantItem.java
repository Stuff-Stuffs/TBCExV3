package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;

import java.util.Collection;

public interface BattleParticipantItem {
    BattleParticipantItemType<?> type();

    BattleParticipantItemRarity rarity();

    OrderedText name(BattleParticipantStateView stateView);

    OrderedText description(BattleParticipantStateView stateView);

    Collection<ItemStack> toItemStacks(BattleParticipantItemStack stack);

    boolean matches(BattleParticipantItem other);
}
