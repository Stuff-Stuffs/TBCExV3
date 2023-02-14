package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Consumer;

//TODO tags?
public interface BattleParticipantAction {
    Text name(BattleParticipantStateView state);

    TooltipText description(BattleParticipantStateView state);

    BattleParticipantActionBuilder<?> builder(BattleParticipantStateView state, Consumer<BattleAction> consumer);

    Optional<Identifier> renderer(BattleParticipantStateView state);
}
