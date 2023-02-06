package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.Text;

public interface BattleParticipantActionTarget {
    BattleParticipantActionTargetType<?> type();

    Text name();

    TooltipText description();
}
