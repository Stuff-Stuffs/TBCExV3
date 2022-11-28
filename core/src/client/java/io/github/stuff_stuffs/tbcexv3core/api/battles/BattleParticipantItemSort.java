package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import net.minecraft.text.OrderedText;

public interface BattleParticipantItemSort {
    int compare(BattleParticipantItemStack first, BattleParticipantItemStack second, BattleParticipantStateView view);

    OrderedText name();
}
