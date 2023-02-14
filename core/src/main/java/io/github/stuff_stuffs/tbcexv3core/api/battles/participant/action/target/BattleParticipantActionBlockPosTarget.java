package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantActionBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.util.TooltipText;
import io.github.stuff_stuffs.tbcexv3util.api.util.RaycastUtil;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public record BattleParticipantActionBlockPosTarget(
        BlockPos pos,
        Text name,
        TooltipText description
) implements BattleParticipantActionTarget {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BattleParticipantActionBlockPosTarget target)) {
            return false;
        }

        return pos.equals(target.pos);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

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

    public static BattleParticipantActionBuilder.TargetRaycaster<BattleParticipantActionBlockPosTarget> filterRaycast(final Predicate<BlockPos> predicate, final RaycastStopper stopper, final Function<BlockPos, Text> nameFunction, final Function<BlockPos, TooltipText> description, final Consumer<BattleParticipantActionTarget> consumer) {
        return new BattleParticipantActionBuilder.TargetRaycaster<>() {
            @Override
            public Optional<? extends Pair<? extends BattleParticipantActionBlockPosTarget, Double>> raycast(final Vec3d start, final Vec3d end) {
                final Optional<? extends Pair<? extends BattleParticipantActionBlockPosTarget, Double>> query = query(start, end);
                query.ifPresent(pair -> consumer.accept(pair.getFirst()));
                return query;
            }

            @Override
            public Optional<? extends Pair<? extends BattleParticipantActionBlockPosTarget, Double>> query(final Vec3d start, final Vec3d end) {
                final Optional<Pair<BlockPos, Double>> p = RaycastUtil.rayCast(start, end, pos -> {
                    if (predicate.test(pos)) {
                        final Optional<Vec3d> raycast = Box.of(pos.toCenterPos(), 1, 1, 1).raycast(start, end);
                        if (raycast.isPresent()) {
                            return Optional.of(Pair.of(pos.toImmutable(), raycast.get().distanceTo(start)));
                        }
                    }
                    return stopper.stop(pos, start, end) ? Optional.empty() : null;
                });
                if (p != null) {
                    return p.map(pair -> Pair.of(new BattleParticipantActionBlockPosTarget(pair.getFirst(), nameFunction.apply(pair.getFirst()), description.apply(pair.getFirst())), pair.getSecond()));
                } else {
                    return Optional.empty();
                }
            }
        };
    }

    public static BattleParticipantActionBuilder.TargetIterator<BattleParticipantActionBlockPosTarget> filterIterator(final Predicate<BlockPos> predicate, final BlockPos center, final int radius, final Function<BlockPos, Text> nameFunction, final Function<BlockPos, TooltipText> description, final Consumer<BattleParticipantActionTarget> consumer) {
        final List<BattleParticipantActionBlockPosTarget> targets = new ArrayList<>();
        for (final BlockPos outward : BlockPos.iterateOutwards(center, radius, radius, radius)) {
            if (predicate.test(outward)) {
                targets.add(new BattleParticipantActionBlockPosTarget(outward.toImmutable(), nameFunction.apply(outward), description.apply(outward)));
            }
        }
        return BattleParticipantActionBuilder.TargetIterator.of(targets.iterator(), consumer);
    }

    public static BattleParticipantActionBuilder.TargetIterator<BattleParticipantActionBlockPosTarget> computed(final Set<BlockPos> data, final Function<BlockPos, Text> nameFunction, final Function<BlockPos, TooltipText> description, final Consumer<BattleParticipantActionTarget> consumer) {
        final List<BattleParticipantActionBlockPosTarget> targets = new ArrayList<>(data.size());
        for (final BlockPos datum : data) {
            targets.add(new BattleParticipantActionBlockPosTarget(datum, nameFunction.apply(datum), description.apply(datum)));
        }
        return BattleParticipantActionBuilder.TargetIterator.of(targets.iterator(), consumer);
    }

    public interface RaycastStopper {
        boolean stop(BlockPos pos, Vec3d start, Vec3d end);
    }
}
