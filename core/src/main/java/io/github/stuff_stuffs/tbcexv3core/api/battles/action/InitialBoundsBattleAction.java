package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

import java.util.Optional;

public class InitialBoundsBattleAction implements BattleAction {
    public static final Codec<InitialBoundsBattleAction> CODEC = BattleBounds.CODEC.xmap(InitialBoundsBattleAction::new, action -> action.bounds);
    private final BattleBounds bounds;

    public InitialBoundsBattleAction(final BattleBounds bounds) {
        this.bounds = bounds;
    }

    @Override
    public BattleActionType<?> getType() {
        return CoreBattleActions.INITIAL_BOUNDS_ACTION;
    }

    @Override
    public Optional<BattleParticipantHandle> getActor() {
        return Optional.empty();
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
        final BattleStatePhase phase = state.getPhase();
        if (phase != BattleStatePhase.INITIALIZATION) {
            throw new TBCExException("Tried to set up initial bounds while not in initialization phase!");
        }
        if (!state.setBattleBounds(bounds, trace)) {
            throw new TBCExException("Failed to setup initial bounds!");
        }
    }
}
