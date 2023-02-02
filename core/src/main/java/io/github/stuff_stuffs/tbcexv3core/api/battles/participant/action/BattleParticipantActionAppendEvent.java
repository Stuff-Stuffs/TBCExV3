package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface BattleParticipantActionAppendEvent {
    Event<BattleParticipantActionAppendEvent> EVENT = EventFactory.createArrayBacked(BattleParticipantActionAppendEvent.class, events -> (state, source, action) -> {
        for (final BattleParticipantActionAppendEvent event : events) {
            if (!event.accept(state, source, action)) {
                return false;
            }
        }
        return true;
    });

    boolean accept(BattleParticipantStateView state, BattleParticipantActionSource source, BattleParticipantAction action);
}
