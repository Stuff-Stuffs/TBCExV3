package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat.BattleParticipantStatMapImpl;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.gen.EventViewable;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
@EventViewable(viewClass = BattleParticipantStatMapView.class)
public interface BattleParticipantStatMap extends BattleParticipantStatMapView {
    BattleParticipantStatModifierKey addModifier(BattleParticipantStat stat, BattleParticipantStatModifier modifier, Tracer<ActionTrace> tracer);

    static BattleParticipantStatMap create() {
        return new BattleParticipantStatMapImpl();
    }
}
