package io.github.stuff_stuffs.tbcexv3core.api.entity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface BattlePlayerComponentEvent {
    Event<BattlePlayerComponentEvent> EVENT = EventFactory.createArrayBacked(BattlePlayerComponentEvent.class, battlePlayerComponentEvents -> builder -> {
        for (BattlePlayerComponentEvent event : battlePlayerComponentEvents) {
            event.onStateBuilder(builder);
        }
    });

    void onStateBuilder(BattleParticipantStateBuilder builder);
}
