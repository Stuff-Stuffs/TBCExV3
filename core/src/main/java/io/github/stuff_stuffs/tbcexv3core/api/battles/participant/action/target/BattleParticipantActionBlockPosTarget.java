package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public record BattleParticipantActionBlockPosTarget(
        BlockPos pos,
        Text name,
        TooltipText description
) implements BattleParticipantActionTarget {
    @Override
    public BattleParticipantActionTargetType<?> type() {
        return CoreBattleActionTargetTypes.BLOCK_POS_TARGET_TYPE;
    }

    @Override
    public Text name() {
        return name;
    }

    @Override
    public TooltipText description() {
        return description;
    }
}
