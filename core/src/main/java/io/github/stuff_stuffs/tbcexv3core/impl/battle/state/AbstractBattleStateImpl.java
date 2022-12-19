package io.github.stuff_stuffs.tbcexv3core.impl.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;

public interface AbstractBattleStateImpl extends BattleState {
    void setup(BattleHandle handle);

    boolean setTeam(BattleParticipantHandle handle, BattleParticipantTeam team, Tracer<ActionTrace> tracer);
}
