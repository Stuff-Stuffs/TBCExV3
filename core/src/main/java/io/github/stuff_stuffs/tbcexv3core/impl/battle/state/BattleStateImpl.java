package io.github.stuff_stuffs.tbcexv3core.impl.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironment;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.CoreBattleParticipantEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.turn.TurnSelector;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.state.AbstractBattleParticipantState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.EventMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

//TODO phase range check
public class BattleStateImpl implements AbstractBattleStateImpl {
    private final EventMap events;
    private final Map<BattleEffectType<?, ?>, BattleEffect> effects;
    private final ParticipantContainer participantContainer;
    private final BattleStateMode mode;
    private TurnSelector turnSelector = TurnSelector.any();
    private BattleHandle handle;
    private BattleBounds bounds;
    private BattleStatePhase phase;
    private BattleEnvironment environment;

    public BattleStateImpl(final BattleStateMode mode) {
        this.mode = mode;
        final EventMap.Builder builder = EventMap.Builder.create();
        BattleState.BATTLE_EVENT_INITIALIZATION_EVENT.invoker().addEvents(builder);
        events = builder.build();
        participantContainer = new ParticipantContainer(events);
        effects = new Reference2ReferenceOpenHashMap<>();
        bounds = BattleBounds.ZERO_ORIGIN;
        phase = BattleStatePhase.SETUP;
    }

    @Override
    public void setup(final BattleHandle handle, final BattleEnvironment environment) {
        checkPhase(BattleStatePhase.SETUP, true);
        this.handle = handle;
        phase = BattleStatePhase.INITIALIZATION;
        this.environment = environment;
        turnSelector.init(this);
    }

    @Override
    public boolean setTeam(final BattleParticipantHandle handle, final BattleParticipantTeam team, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.SETUP, false);
        if (!team.getOwner().equals(this.handle)) {
            throw new TBCExException("Team owner mismatch!");
        }
        return participantContainer.setTeam(handle, team, tracer);
    }

    @Override
    public void setTurnSelector(final TurnSelector turnSelector) {
        checkPhase(BattleStatePhase.INITIALIZATION, true);
        this.turnSelector.deinit();
        this.turnSelector = turnSelector;
    }

    @Override
    public void ready() {
        checkPhase(BattleStatePhase.INITIALIZATION, true);
        phase = BattleStatePhase.FIGHT;
        for (final BattleParticipantHandle participant : getParticipants()) {
            participantContainer.getParticipantByHandle(participant).ready();
        }
    }

    @Override
    public EventMap getEventMap() {
        checkPhase(BattleStatePhase.SETUP, false);
        return events;
    }

    @Override
    public BattleStatePhase getPhase() {
        return phase;
    }

    @Override
    public BattleStateMode getMode() {
        return mode;
    }

    @Override
    public boolean hasEffect(final BattleEffectType<?, ?> type) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        return effects.containsKey(type);
    }

    @Override
    public <View extends BattleEffect> Optional<View> getEffectView(final BattleEffectType<View, ?> type) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        final BattleEffect effect = effects.get(type);
        return Optional.ofNullable((View) effect);
    }

    @Override
    public BattleBounds getBattleBounds() {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        return bounds;
    }

    @Override
    public Iterable<BattleParticipantHandle> getParticipants() {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        return participantContainer.getParticipants();
    }

    @Override
    public Stream<BattleParticipantHandle> getParticipantStream() {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        return participantContainer.getParticipantStream();
    }

    @Override
    public <View extends BattleEffect, Effect extends View> Optional<Effect> getEffect(final BattleEffectType<View, Effect> type) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        final BattleEffect effect = effects.get(type);
        return Optional.ofNullable((Effect) effect);
    }

    @Override
    public void removeEffect(final BattleEffectType<?, ?> type, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        final BattleEffect removed = effects.remove(type);
        if (removed == null) {
            throw new TBCExException();
        }
        tracer.pushInstant(true).value(new BattleActionTraces.BattleRemoveEffect(removed)).buildAndApply();
        removed.deinit(tracer);
        tracer.pop();
    }

    @Override
    public void addEffect(final BattleEffect effect, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        if (effects.put(effect.getType(), effect) != null) {
            throw new TBCExException();
        }
        tracer.pushInstant(true).value(new BattleActionTraces.BattleAddEffect(effect)).buildAndApply();
        effect.init(this, tracer);
        tracer.pop();
    }

    @Override
    public boolean setBattleBounds(final BattleBounds bounds, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        if (events.getEvent(CoreBattleEvents.PRE_BATTLE_BOUNDS_SET).getInvoker().preBattleBoundsSet(this, bounds, tracer)) {
            final BattleBounds old = this.bounds;
            this.bounds = bounds;
            tracer.pushInstant(true).value(new BattleActionTraces.BattleSetBounds(Optional.ofNullable(old), bounds)).buildAndApply();
            events.getEvent(CoreBattleEvents.SUCCESSFUL_BATTLE_BOUNDS_SET).getInvoker().successfulBattleBoundsSet(this, old, tracer);
            tracer.pop();
            return true;
        }
        events.getEvent(CoreBattleEvents.FAILED_BATTLE_BOUNDS_SET).getInvoker().failedBattleBoundsSet(this, bounds, tracer);
        return false;
    }

    @Override
    public BattleParticipantState getParticipantByHandle(final BattleParticipantHandle handle) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        return participantContainer.getParticipantByHandle(handle);
    }

    @Override
    public Optional<BattleParticipantTeam> getTeamById(final Identifier id) {
        return participantContainer.getTeamById(id);
    }

    @Override
    public BattleParticipantTeamRelation getTeamRelation(final BattleParticipantTeam first, final BattleParticipantTeam second) {
        return participantContainer.getTeamRelation(first, second);
    }

    @Override
    public Iterable<BattleParticipantHandle> getParticipantsByTeam(final BattleParticipantTeam team) {
        return participantContainer.getParticipantsByTeam(team);
    }

    @Override
    public BattleParticipantTeam getTeamByParticipant(final BattleParticipantHandle handle) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        if (!handle.getParent().equals(this.handle)) {
            throw new TBCExException("Tried to get participant from another battle!");
        }
        return participantContainer.getTeam(handle);
    }

    @Override
    public BattleHandle getHandle() {
        return handle;
    }

    @Override
    public boolean isCurrentTurn(final BattleParticipantHandle handle) {
        return turnSelector.isCurrentTurn(this, handle);
    }

    @Override
    public Optional<BattleParticipantHandle> addParticipant(final BattleParticipantState participant, final BattleParticipantTeam team, final Optional<BattleParticipantHandle> invitation, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        if (!bounds.isIn(participant.getBounds())) {
            return Optional.empty();
        }
        if (participant.getPhase().getOrder() > BattleParticipantStatePhase.SETUP.getOrder()) {
            throw new TBCExException();
        }
        if (!team.getOwner().equals(handle)) {
            throw new TBCExException("Team owner mismatch!");
        }
        if (!(participant instanceof AbstractBattleParticipantState)) {
            throw new TBCExException();
        }
        if (invitation.isEmpty()) {
            if (phase.getOrder() > BattleStatePhase.INITIALIZATION.getOrder()) {
                throw new TBCExException();
            }
        }
        final Optional<BattleParticipantHandle> handle = participantContainer.addParticipant(participant, team, tracer, this.handle);
        handle.ifPresentOrElse(battleParticipantHandle -> {
            participantContainer.getParticipantByHandle(battleParticipantHandle).setup(this);
            if (phase == BattleStatePhase.FIGHT) {
                participantContainer.getParticipantByHandle(battleParticipantHandle).ready();
            }
            events.getEvent(CoreBattleEvents.SUCCESSFUL_BATTLE_PARTICIPANT_JOIN).getInvoker().successfulBattleParticipantJoin(participant, tracer);
            tracer.pop();
        }, () -> {
            events.getEvent(CoreBattleEvents.FAILED_BATTLE_PARTICIPANT_JOIN).getInvoker().failedBattleParticipantJoin(participant, tracer);
        });
        return handle;
    }

    @Override
    public boolean removeParticipant(final BattleParticipantHandle handle, final BattleParticipantRemovalReason reason, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.FIGHT, false);
        final boolean removed = participantContainer.removeParticipant(handle, reason, tracer, this);
        if (removed) {
            if (participantContainer.canEnd()) {
                phase = BattleStatePhase.FINISHED;
                events.getEvent(CoreBattleEvents.BATTLE_END).getInvoker().battleEnd(this, tracer);
            }
        }
        return removed;
    }

    @Override
    public boolean setTeamRelation(final BattleParticipantTeam first, final BattleParticipantTeam second, final BattleParticipantTeamRelation relation, final Tracer<ActionTrace> tracer) {
        if (first.equals(second)) {
            throw new IllegalArgumentException("Cannot set the relation of a team to itself!");
        }
        if (!first.getOwner().equals(handle) || !second.getOwner().equals(handle)) {
            throw new IllegalArgumentException("Tried to set a team relation from a different battle!");
        }
        return participantContainer.setTeamRelation(first, second, relation, tracer, this);
    }

    @Override
    public BattleParticipantTeam addTeam(final Identifier identifier) {
        return participantContainer.addTeam(identifier, handle);
    }

    @Override
    public boolean removeTeam(final BattleParticipantTeam team) {
        if (!team.getOwner().equals(handle)) {
            throw new TBCExException("Team owner mismatch!");
        }
        return participantContainer.removeTeam(team);
    }

    @Override
    public BattleEnvironment getEnvironment() {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        return environment;
    }

    private void checkPhase(final BattleStatePhase phase, final boolean exact) {
        if ((exact && phase != this.phase) || (!exact && this.phase.getOrder() < phase.getOrder())) {
            throw new TBCExException("Phase is " + this.phase);
        }
    }
}
