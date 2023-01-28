package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

import java.util.Optional;

public class BattleParticipantLeaveBattleAction implements BattleAction {
    public static final Codec<BattleParticipantLeaveBattleAction> CODEC = BattleParticipantHandle.codec().xmap(BattleParticipantLeaveBattleAction::new, action -> action.handle);
    private final BattleParticipantHandle handle;

    public BattleParticipantLeaveBattleAction(final BattleParticipantHandle handle) {
        this.handle = handle;
    }

    @Override
    public BattleActionType<?> getType() {
        return CoreBattleActions.BATTLE_PARTICIPANT_LEAVE_ACTION;
    }

    @Override
    public Optional<BattleParticipantHandle> getActor() {
        return Optional.empty();
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
        final boolean removed = state.removeParticipant(handle, BattleParticipantRemovalReason.LEFT, trace);
        if (!removed) {
            throw new TBCExException("Tried to leave battle but couldn't!");
        }
    }
}
