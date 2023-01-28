package io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;

import java.util.Optional;

public final class BattleActionTraces {
    private BattleActionTraces() {
    }

    public record BattleAddEffect(
            BattleEffect effect
    ) implements ActionTrace {
    }

    public record BattleRemoveEffect(
            BattleEffect effect
    ) implements ActionTrace {
    }


    public record BattleSetBounds(
            Optional<BattleBounds> oldBounds,
            BattleBounds newBounds
    ) implements ActionTrace {
    }

    public record BattleTeamSetRelation(
            BattleParticipantTeam first,
            BattleParticipantTeam second,
            BattleParticipantTeamRelation oldRelation,
            BattleParticipantTeamRelation newRelation
    ) implements ActionTrace {
    }
}
