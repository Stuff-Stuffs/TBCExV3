package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Optional;

public class BattleParticipantMoveBattleAction implements BattleAction {
    public static final Codec<BattleParticipantMoveBattleAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.listOf().fieldOf("destination").forGetter(action -> action.destination),
            BattleParticipantHandle.codec().fieldOf("target").forGetter(action -> action.target)
    ).apply(instance, BattleParticipantMoveBattleAction::new));
    private final List<BlockPos> destination;
    private final BattleParticipantHandle target;

    public BattleParticipantMoveBattleAction(final List<BlockPos> destination, final BattleParticipantHandle target) {
        this.destination = destination;
        this.target = target;
    }

    @Override
    public BattleActionType<?> getType() {
        return CoreBattleActions.BATTLE_PARTICIPANT_MOVE_ACTION;
    }

    @Override
    public Optional<BattleParticipantHandle> getActor() {
        return Optional.of(target);
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
        final BattleParticipantState target = state.getParticipantByHandle(this.target);
        for (final BlockPos pos : destination) {
            if (!target.setPosition(pos, trace)) {
                throw new RuntimeException();
            }
        }
    }
}
