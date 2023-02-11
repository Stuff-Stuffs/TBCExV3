package io.github.stuff_stuffs.tbcexv3core.api.battles.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironment;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.turn.TurnSelector;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event.EventMap;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.BattleStateImpl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.NonExtendable
public interface BattleState extends BattleStateView {
    void setTurnSelector(TurnSelector turnSelector);

    void ready();

    @Override
    EventMap getEventMap();

    <View extends BattleEffect, Effect extends View> Optional<Effect> getEffect(BattleEffectType<View, Effect> type);

    void removeEffect(BattleEffectType<?, ?> type, Tracer<ActionTrace> tracer);

    void addEffect(BattleEffect effect, Tracer<ActionTrace> tracer);

    boolean setBattleBounds(BattleBounds bounds, Tracer<ActionTrace> tracer);

    @Override
    BattleParticipantState getParticipantByHandle(BattleParticipantHandle handle);

    Optional<BattleParticipantHandle> addParticipant(BattleParticipantState participant, BattleParticipantTeam team, Optional<BattleParticipantHandle> invitation, Tracer<ActionTrace> tracer);

    boolean removeParticipant(BattleParticipantHandle handle, BattleParticipantRemovalReason reason, Tracer<ActionTrace> tracer);

    boolean setTeamRelation(BattleParticipantTeam first, BattleParticipantTeam second, BattleParticipantTeamRelation relation, Tracer<ActionTrace> tracer);

    BattleParticipantTeam addTeam(Identifier identifier);

    boolean removeTeam(BattleParticipantTeam team);

    @Override
    BattleEnvironment getEnvironment();

    Event<EventInitializer> BATTLE_EVENT_INITIALIZATION_EVENT = EventFactory.createArrayBacked(EventInitializer.class, eventInitializers -> builder -> {
        for (EventInitializer initializer : eventInitializers) {
            initializer.addEvents(builder);
        }
    });

    interface EventInitializer {
        void addEvents(EventMap.Builder builder);
    }

    static BattleState createEmpty(final BattleStateMode mode) {
        return new BattleStateImpl(mode);
    }
}
