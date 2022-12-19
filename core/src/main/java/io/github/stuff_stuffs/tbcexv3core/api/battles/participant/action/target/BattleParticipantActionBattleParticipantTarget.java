package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public record BattleParticipantActionBattleParticipantTarget(
        BattleParticipantStateView state,
        OrderedText name,
        TooltipText description
) implements BattleParticipantActionTarget {
    @Override
    public BattleParticipantActionTargetType<?> type() {
        return CoreBattleActionTargetTypes.BATTLE_PARTICIPANT_TARGET_TYPE;
    }

    @Override
    public OrderedText name(final BattleParticipantStateView stateView) {
        return name;
    }

    @Override
    public TooltipText description(final BattleParticipantStateView stateView) {
        return description;
    }

    public static BattleParticipantActionBuilder.TargetIterator<BattleParticipantActionBattleParticipantTarget> allIterator(final BattleParticipantStateView stateView, final Function<BattleParticipantStateView, OrderedText> nameFunction, final Function<BattleParticipantStateView, TooltipText> descriptionFunction, final Consumer<BattleParticipantActionTarget> consumer, final BooleanSupplier valid) {
        return filterIterator(stateView, nameFunction, descriptionFunction, consumer, valid, i -> true);
    }

    public static BattleParticipantActionBuilder.TargetIterator<BattleParticipantActionBattleParticipantTarget> filterIterator(final BattleParticipantStateView stateView, final Function<BattleParticipantStateView, OrderedText> nameFunction, final Function<BattleParticipantStateView, TooltipText> descriptionFunction, final Consumer<BattleParticipantActionTarget> consumer, final BooleanSupplier valid, final Predicate<BattleParticipantStateView> predicate) {
        final BattleStateView view = stateView.getBattleState();
        return BattleParticipantActionBuilder.TargetIterator.of(view.getParticipantStream().map(view::getParticipantByHandle).filter(predicate).map(state -> new BattleParticipantActionBattleParticipantTarget(state, nameFunction.apply(state), descriptionFunction.apply(state))).iterator(), consumer, valid);
    }

    public static BattleParticipantActionBuilder.TargetRaycaster<BattleParticipantActionBattleParticipantTarget> allRaycast(final BattleParticipantStateView stateView, final Function<BattleParticipantStateView, OrderedText> nameFunction, final Function<BattleParticipantStateView, TooltipText> descriptionFunction, final Consumer<BattleParticipantActionTarget> consumer, final BooleanSupplier valid, final Vec3d start, final Vec3d end) {
        return filterRaycast(stateView, nameFunction, descriptionFunction, consumer, valid, i -> true, start, end);
    }

    public static BattleParticipantActionBuilder.TargetRaycaster<BattleParticipantActionBattleParticipantTarget> filterRaycast(final BattleParticipantStateView stateView, final Function<BattleParticipantStateView, OrderedText> nameFunction, final Function<BattleParticipantStateView, TooltipText> descriptionFunction, final Consumer<BattleParticipantActionTarget> consumer, final BooleanSupplier valid, final Predicate<BattleParticipantStateView> predicate, final Vec3d start, final Vec3d end) {
        final BattleStateView view = stateView.getBattleState();
        return BattleParticipantActionBuilder.TargetRaycaster.of(view.getParticipantStream().map(view::getParticipantByHandle).filter(predicate).map(state -> new BattleParticipantActionBattleParticipantTarget(state, nameFunction.apply(state), descriptionFunction.apply(state))).toList(), target -> {
            final BattleParticipantBounds bounds = target.state.getBounds();
            final Stream.Builder<BattleParticipantBounds.Part> builder = Stream.builder();
            final Iterator<BattleParticipantBounds.Part> parts = bounds.parts();
            while (parts.hasNext()) {
                builder.add(parts.next());
            }
            return builder.build().map(BattleParticipantBounds.Part::box).iterator();
        }, consumer, valid, start, end);
    }
}
