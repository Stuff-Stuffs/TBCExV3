package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state;

import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.BattleParticipantInventoryView;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMapView;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.NonExtendable
public interface BattleParticipantStateView {
    EventMapView getEventMap();

    <View extends BattleParticipantEffect> View getEffectView(BattleParticipantEffectType<View, ?> type);

    BattleParticipantInventoryView getInventory();

    BattleStateView getBattleState();

    UUID getUuid();

    BattleParticipantStatePhase getPhase();

    BattleParticipantHandle getHandle();
}
