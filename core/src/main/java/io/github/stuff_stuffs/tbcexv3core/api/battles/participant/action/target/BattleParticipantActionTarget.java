package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.OrderedText;

public interface BattleParticipantActionTarget {
    BattleParticipantActionTargetType<?> type();

    OrderedText name(BattleParticipantStateView stateView);

    TooltipText description(BattleParticipantStateView stateView);
}
