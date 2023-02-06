package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public interface BattleParticipantItem {
    BattleParticipantItemType<?> type();

    BattleParticipantItemRarity rarity();

    Text name(BattleParticipantStateView stateView);

    TooltipText description(BattleParticipantStateView stateView);

    List<ItemStack> toItemStacks(BattleParticipantItemStack stack);

    boolean matches(BattleParticipantItem other);

    default List<BattleParticipantAction> actions(final BattleParticipantStateView stateView, final BattleParticipantItemStack stack, final Optional<BattleParticipantInventoryHandle> handle) {
        return List.of();
    }

    default Optional<TagKey<BattleParticipantEquipmentSlot>> getAcceptableSlots(final BattleParticipantStateView stateView) {
        return Optional.empty();
    }
}
