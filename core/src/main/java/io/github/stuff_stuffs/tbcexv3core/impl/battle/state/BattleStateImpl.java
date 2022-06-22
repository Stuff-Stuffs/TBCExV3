package io.github.stuff_stuffs.tbcexv3core.impl.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;

public class BattleStateImpl implements BattleState {
    private final EventMap events;

    public BattleStateImpl() {
        EventMap.Builder builder = EventMap.Builder.create();
        BattleState.BATTLE_EVENT_INITIALIZATION_EVENT.invoker().addEvents(builder);
        events = builder.build();
    }

    @Override
    public EventMap getEventMap() {
        return events;
    }
}
