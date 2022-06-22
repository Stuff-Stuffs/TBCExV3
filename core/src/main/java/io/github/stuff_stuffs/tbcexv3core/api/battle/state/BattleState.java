package io.github.stuff_stuffs.tbcexv3core.api.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battle.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battle.effect.BattleEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.BattleStateImpl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleState extends BattleStateView {
    @Override
    EventMap getEventMap();

    <View extends BattleEffect, Effect extends View> Effect getEffect(BattleEffectType<View, Effect> type);

    void removeEffect(BattleEffectType<?, ?> type, Tracer<ActionTrace> tracer);

    void addEffect(BattleEffect effect, Tracer<ActionTrace> tracer);

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
