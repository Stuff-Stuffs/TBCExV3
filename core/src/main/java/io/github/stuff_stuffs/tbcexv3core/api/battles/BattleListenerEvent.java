package io.github.stuff_stuffs.tbcexv3core.api.battles;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;

public interface BattleListenerEvent {
    Event<BattleListenerEvent> EVENT = EventFactory.createArrayBacked(BattleListenerEvent.class, events -> (view, serverWorld) -> {
        for (final BattleListenerEvent event : events) {
            event.attachListeners(view, serverWorld);
        }
    });

    void attachListeners(BattleView view, ServerWorld world);
}
