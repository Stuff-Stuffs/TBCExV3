package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

import java.util.Optional;

public class InitialParticipantJoinBattleAction implements BattleAction {
    public static final Codec<InitialParticipantJoinBattleAction> CODEC = BattleParticipantStateBuilder.Built.codec(false).xmap(InitialParticipantJoinBattleAction::new, action -> action.built);
    public static final Codec<InitialParticipantJoinBattleAction> NETWORK_CODEC = BattleParticipantStateBuilder.Built.codec(true).xmap(InitialParticipantJoinBattleAction::new, action -> action.built);
    private final BattleParticipantStateBuilder.Built built;

    public InitialParticipantJoinBattleAction(final BattleParticipantStateBuilder.Built built) {
        this.built = built;
    }

    @Override
    public BattleActionType<?> getType() {
        return CoreBattleActions.INITIAL_PARTICIPANT_JOIN_ACTION;
    }

    @Override
    public Optional<BattleParticipantHandle> getActor() {
        return Optional.empty();
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
        if (state.getPhase() != BattleStatePhase.INITIALIZATION) {
            throw new TBCExException("Wrong phase!");
        }
        final BattleParticipantState participantState = BattleParticipantState.create(built.getUuid(), built.getComponents());
        if (!state.addParticipant(participantState, Optional.empty(), trace)) {
            throw new TBCExException("Problem adding participant during initialization!");
        }
        if (participantState.getPhase() != BattleParticipantStatePhase.INITIALIZATION) {
            throw new TBCExException("Error, participant initialized before it should have been!");
        }
        built.forEach(participantState);
    }
}
