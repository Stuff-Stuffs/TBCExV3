package io.github.stuff_stuffs.tbcexv3_test.common.item;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemRarity;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class TestBattleParticipantItem implements BattleParticipantItem {
    public static final Codec<TestBattleParticipantItem> CODEC = Codec.LONG.xmap(TestBattleParticipantItem::new, item -> item.id);
    private final long id;

    public TestBattleParticipantItem(final long id) {
        this.id = id;
    }

    @Override
    public BattleParticipantItemType<?> type() {
        return TestBattleParticipantItemTypes.TEST_ITEM_TYPE;
    }

    @Override
    public BattleParticipantItemRarity rarity() {
        final Random random = Random.create(id);
        final BattleParticipantItemRarity.RarityClass[] values = BattleParticipantItemRarity.RarityClass.values();
        final BattleParticipantItemRarity.RarityClass rarityClass = values[random.nextInt(values.length)];
        return new BattleParticipantItemRarity(random.nextDouble(), rarityClass);
    }

    @Override
    public Text name(final BattleParticipantStateView stateView) {
        return Text.of(Long.toString(id, 16));
    }

    @Override
    public TooltipText description(final BattleParticipantStateView stateView) {
        return TooltipText.EMPTY;
    }

    @Override
    public Collection<ItemStack> toItemStacks(final BattleParticipantItemStack stack) {
        return Collections.emptyList();
    }

    @Override
    public boolean matches(final BattleParticipantItem other) {
        if (other.type() == type() && other instanceof TestBattleParticipantItem item) {
            return id == item.id;
        }
        return false;
    }

    @Override
    public Optional<TagKey<BattleParticipantEquipmentSlot>> getAcceptableSlots(final BattleParticipantStateView stateView) {
        return Optional.empty();
    }
}
