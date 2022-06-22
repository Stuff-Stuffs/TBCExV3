package io.github.stuff_stuffs.tbcexv3core.api.battle.participant;

import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;

public interface BattleParticipantState extends BattleParticipantStateView {
    @Override
    EventMap getEventMap();
}
