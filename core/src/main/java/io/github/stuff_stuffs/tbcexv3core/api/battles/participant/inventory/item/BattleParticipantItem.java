package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;
import net.minecraft.text.OrderedText;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BattleParticipantItem {
    BattleParticipantItemType<?> type();

    BattleParticipantItemRarity rarity();

    OrderedText name(BattleParticipantStateView stateView);

    TooltipText description(BattleParticipantStateView stateView);

    Collection<ItemStack> toItemStacks(BattleParticipantItemStack stack);

    boolean matches(BattleParticipantItem other);

    default Optional<TagKey<BattleParticipantEquipmentSlot>> getAcceptableSlots(final BattleParticipantStateView stateView) {
        return Optional.empty();
    }
}
