package io.github.stuff_stuffs.tbcexv3core.api.battles.item;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import net.minecraft.text.OrderedText;

public interface BattleParticipantItemFilter {
    boolean accepted(BattleParticipantItemStack stack, BattleParticipantStateView view);

    OrderedText name();
}
