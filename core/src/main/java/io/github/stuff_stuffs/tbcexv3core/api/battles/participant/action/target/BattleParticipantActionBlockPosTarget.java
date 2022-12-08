package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.BlockPos;

public record BattleParticipantActionBlockPosTarget(
        BlockPos pos,
        OrderedText name,
        TooltipText description
) implements BattleParticipantActionTarget {
    @Override
    public BattleParticipantActionTargetType<?> type() {
        return CoreBattleActionTargetTypes.BLOCK_POS_TARGET_TYPE;
    }

    @Override
    public OrderedText name(final BattleParticipantStateView stateView) {
        return name;
    }

    @Override
    public TooltipText description(final BattleParticipantStateView stateView) {
        return description;
    }
}
