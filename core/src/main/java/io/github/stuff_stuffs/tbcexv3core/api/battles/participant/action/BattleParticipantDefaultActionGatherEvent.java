package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.function.Consumer;

public interface BattleParticipantDefaultActionGatherEvent {
    Event<BattleParticipantDefaultActionGatherEvent> EVENT = EventFactory.createArrayBacked(BattleParticipantDefaultActionGatherEvent.class, events -> (state, actionConsumer) -> {
        for (BattleParticipantDefaultActionGatherEvent event : events) {
            event.gather(state, actionConsumer);
        }
    });

    void gather(BattleParticipantStateView state, Consumer<BattleParticipantAction> actionConsumer);
}
