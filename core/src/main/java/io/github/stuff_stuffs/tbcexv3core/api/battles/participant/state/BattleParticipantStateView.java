package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatMapView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMapView;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.UUID;

@ApiStatus.NonExtendable
public interface BattleParticipantStateView {
    EventMapView getEventMap();

    <View extends BattleParticipantEffect> Optional<View> getEffectView(BattleParticipantEffectType<View, ?> type);

    BattleParticipantInventoryView getInventory();

    BattleParticipantStatMapView getStatMap();

    BattleStateView getBattleState();

    UUID getUuid();

    BattleParticipantStatePhase getPhase();

    BattleParticipantHandle getHandle();

    <T extends BattleEntityComponent> Optional<T> getEntityComponent(BattleEntityComponentType<T> componentType);

    Iterable<? extends BattleEntityComponent> entityComponents();
}
