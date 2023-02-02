package io.github.stuff_stuffs.tbcexv3core.internal.client;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemFilter;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemFilters;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemSort;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleParticipantItemSorts;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemRarity;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateReceiver;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.EntityBattlesUpdateReceiver;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.PlayerCurrentBattleReceiver;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class TBCExV3CoreClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BattleUpdateReceiver.init();
        EntityBattlesUpdateReceiver.init();
        PlayerCurrentBattleReceiver.init();
        BattleParticipantItemFilters.instance().register(TBCExV3Core.createId("all"), new BattleParticipantItemFilter() {
            @Override
            public boolean accepted(final BattleParticipantItemStack stack, final BattleParticipantStateView view) {
                return true;
            }

            @Override
            public OrderedText name() {
                return Text.of("ALL").asOrderedText();
            }
        }, view -> true);
        BattleParticipantItemSorts.instance().register(TBCExV3Core.createId("count"), new BattleParticipantItemSort() {
            @Override
            public int compare(final BattleParticipantItemStack first, final BattleParticipantItemStack second, final BattleParticipantStateView view) {
                return Integer.compare(first.getCount(), second.getCount());
            }

            @Override
            public OrderedText name() {
                return Text.of("COUNT").asOrderedText();
            }
        }, view -> true);
        BattleParticipantItemSorts.instance().register(TBCExV3Core.createId("rarity"), new BattleParticipantItemSort() {
            @Override
            public int compare(final BattleParticipantItemStack first, final BattleParticipantItemStack second, final BattleParticipantStateView view) {
                return BattleParticipantItemRarity.COMPARATOR.compare(first.getItem().rarity(), second.getItem().rarity());
            }

            @Override
            public OrderedText name() {
                return Text.of("RARITY").asOrderedText();
            }
        }, view -> true);
    }
}
