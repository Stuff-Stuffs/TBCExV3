package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.OrderedText;

import java.util.function.Consumer;

public interface BattleParticipantAction {
    OrderedText name(BattleParticipantStateView stateView);

    TooltipText description(BattleParticipantStateView stateView);

    BattleParticipantActionBuilder builder(BattleParticipantStateView stateView, Consumer<BattleAction> consumer);
}
