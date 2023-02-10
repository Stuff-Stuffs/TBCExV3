package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public record BattleParticipantActionBattleParticipantTarget(
        BattleParticipantHandle handle,
        Text name,
        TooltipText description
) implements BattleParticipantActionTarget {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BattleParticipantActionBattleParticipantTarget target)) {
            return false;
        }

        return handle.equals(target.handle);
    }

    @Override
    public int hashCode() {
        return handle.hashCode();
    }

    @Override
    public BattleParticipantActionTargetType<?> type() {
        return CoreBattleActionTargetTypes.BATTLE_PARTICIPANT_TARGET_TYPE;
    }

    @Override
    public Text name() {
        return name;
    }

    @Override
    public TooltipText description() {
        return description;
    }

    public static BattleParticipantActionBuilder.RaycastIterator<BattleParticipantActionBattleParticipantTarget> filter(final BattleParticipantStateView stateView, final Predicate<BattleParticipantStateView> predicate, final Function<BattleParticipantStateView, Text> nameFunction, final Function<BattleParticipantStateView, TooltipText> description, final Consumer<BattleParticipantActionTarget> consumer) {
        final BattleStateView state = stateView.getBattleState();
        final List<BattleParticipantActionBattleParticipantTarget> targets = state.getParticipantStream().map(
                state::getParticipantByHandle
        ).filter(
                predicate
        ).map(
                view -> new BattleParticipantActionBattleParticipantTarget(view.getHandle(), nameFunction.apply(view), description.apply(view))
        ).toList();
        return new BattleParticipantActionBuilder.RaycastIterator<>(
                BattleParticipantActionBuilder.TargetRaycaster.of(
                        targets::iterator,
                        handle -> state.getParticipantByHandle(handle.handle()).getBounds().partStream().map(BattleParticipantBounds.Part::box).iterator(), consumer::accept
                ),
                BattleParticipantActionBuilder.TargetIterator.of(
                        targets.iterator(),
                        consumer
                )
        );
    }
}
