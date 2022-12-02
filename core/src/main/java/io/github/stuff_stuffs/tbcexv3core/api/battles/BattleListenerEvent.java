package io.github.stuff_stuffs.tbcexv3core.api.battles;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface BattleListenerEvent {
    Event<BattleListenerEvent> EVENT = EventFactory.createArrayBacked(BattleListenerEvent.class, events -> view -> {
        for (final BattleListenerEvent event : events) {
            event.attachListener(view);
        }
    });

    void attachListener(BattleView view);
}
