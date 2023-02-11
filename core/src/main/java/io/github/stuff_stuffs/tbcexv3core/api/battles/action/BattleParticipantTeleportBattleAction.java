package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleParticipantActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public class BattleParticipantTeleportBattleAction implements BattleAction {
    public static final Codec<BattleParticipantTeleportBattleAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(BlockPos.CODEC.fieldOf("destination").forGetter(action -> action.destination), BattleParticipantHandle.codec().fieldOf("target").forGetter(action -> action.target), BattleParticipantHandle.codec().optionalFieldOf("teleporter").forGetter(action -> action.teleporter)).apply(instance, BattleParticipantTeleportBattleAction::new));
    private final BlockPos destination;
    private final BattleParticipantHandle target;
    private final Optional<BattleParticipantHandle> teleporter;

    public BattleParticipantTeleportBattleAction(final BlockPos destination, final BattleParticipantHandle target) {
        this(destination, target, Optional.of(target));
    }

    public BattleParticipantTeleportBattleAction(final BlockPos destination, final BattleParticipantHandle target, final Optional<BattleParticipantHandle> teleporter) {
        this.destination = destination;
        this.target = target;
        this.teleporter = teleporter;
    }

    @Override
    public BattleActionType<?> getType() {
        return CoreBattleActions.BATTLE_PARTICIPANT_TELEPORT_ACTION;
    }

    @Override
    public Optional<BattleParticipantHandle> getActor() {
        return teleporter;
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
        if (state.getPhase() != BattleStatePhase.FIGHT) {
            throw new RuntimeException();
        }
        final TracerView.IntervalStart<ActionTrace> start = trace.pushStart(true).value(new BattleParticipantActionTraces.BattleParticipantStartMove(target)).buildAndApply();
        state.getParticipantByHandle(target).setPosition(destination, trace);
        trace.pushEnd(start, true).value(new BattleParticipantActionTraces.BattleParticipantEndMove(target)).buildAndApply();
    }
}
