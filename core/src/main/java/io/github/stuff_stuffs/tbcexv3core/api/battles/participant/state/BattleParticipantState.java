package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventory;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatMap;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentMap;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.state.BattleParticipantStateImpl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.UUID;

@ApiStatus.NonExtendable
public interface BattleParticipantState extends BattleParticipantStateView {
    @Override
    EventMap getEventMap();

    <View extends BattleParticipantEffect, Effect extends View> Optional<Effect> getEffect(BattleParticipantEffectType<View, Effect> type);

    void removeEffect(BattleParticipantEffectType<?, ?> type, Tracer<ActionTrace> tracer);

    void addEffect(BattleParticipantEffect effect, Tracer<ActionTrace> tracer);

    @Override
    BattleParticipantInventory getInventory();

    @Override
    BattleParticipantStatMap getStatMap();

    @Override
    BattleState getBattleState();

    Event<EventInitializer> BATTLE_PARTICIPANT_EVENT_INITIALIZATION_EVENT = EventFactory.createArrayBacked(EventInitializer.class, eventInitializers -> builder -> {
        for (EventInitializer initializer : eventInitializers) {
            initializer.addEvents(builder);
        }
    });

    interface EventInitializer {
        void addEvents(EventMap.Builder builder);
    }

    static BattleParticipantState create(final UUID uuid, final BattleEntityComponentMap componentMap) {
        return new BattleParticipantStateImpl(uuid, componentMap);
    }
}
