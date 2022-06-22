package io.github.stuff_stuffs.tbcexv3core.api.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.BattleStateImpl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleState extends BattleStateView {
    @Override
    EventMap getEventMap();

    Event<EventInitializer> BATTLE_EVENT_INITIALIZATION_EVENT = EventFactory.createArrayBacked(EventInitializer.class, eventInitializers -> builder -> {
        for (EventInitializer initializer : eventInitializers) {
            initializer.addEvents(builder);
        }
    });

    interface EventInitializer {
        void addEvents(EventMap.Builder builder);
    }

    static BattleState createEmpty() {
        return new BattleStateImpl();
    }
}
