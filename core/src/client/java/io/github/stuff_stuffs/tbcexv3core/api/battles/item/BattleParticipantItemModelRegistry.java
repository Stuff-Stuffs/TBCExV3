package io.github.stuff_stuffs.tbcexv3core.api.battles.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItem;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemType;
import io.github.stuff_stuffs.tbcexv3core.impl.battles.item.BattleParticipantItemModelRegistryImpl;

import java.util.function.Function;

public interface BattleParticipantItemModelRegistry {
    BattleParticipantItemModelRegistry INSTANCE = new BattleParticipantItemModelRegistryImpl();

    <T extends BattleParticipantItem> void register(BattleParticipantItemType<T> type, Function<T, BattleParticipantItemModel> model);

    BattleParticipantItemModel get(BattleParticipantItem item);
}
