package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.Text;

public interface BattleParticipantEffectRenderInfo<View extends BattleParticipantEffect, Effect extends View> {
    boolean shown(BattleParticipantStateView state);

    Text name(View view, BattleParticipantStateView state);

    TooltipText description(View view, BattleParticipantStateView state);
}
