package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat.BattleParticipantStatMapImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantStatMap extends BattleParticipantStatMapView {
    BattleParticipantStatModifierKey addModifier(BattleParticipantStat stat, Modifier modifier);

    interface Modifier {
        int BASE = 0;
        int ADD = 1;
        int MULTIPLY = 2;
        int POST_MULTIPLY_ADD = 3;
        int LAST = 4;

        int getPriority();

        double modify(double value, Tracer<StatTrace> tracer);
    }

    static BattleParticipantStatMap create() {
        return new BattleParticipantStatMapImpl();
    }
}
