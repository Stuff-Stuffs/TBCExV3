package io.github.stuff_stuffs.tbcexv3core.impl.battle.state;

import com.google.common.collect.Iterables;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.state.AbstractBattleParticipantState;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.util.Map;
import java.util.Optional;

public class BattleStateImpl implements AbstractBattleStateImpl {
    private final EventMap events;
    private final Map<BattleEffectType<?, ?>, BattleEffect> effects;
    private final Map<BattleParticipantHandle, AbstractBattleParticipantState> participantStates;
    private final BattleStateMode mode;
    private BattleHandle handle;
    private BattleBounds bounds;
    private BattleStatePhase phase;

    public BattleStateImpl(final BattleStateMode mode) {
        this.mode = mode;
        final EventMap.Builder builder = EventMap.Builder.create();
        BattleState.BATTLE_EVENT_INITIALIZATION_EVENT.invoker().addEvents(builder);
        events = builder.build();
        participantStates = new Reference2ReferenceOpenHashMap<>();
        effects = new Reference2ReferenceOpenHashMap<>();
        bounds = BattleBounds.ZERO_ORIGIN;
        phase = BattleStatePhase.SETUP;
    }

    @Override
    public void setup(final BattleHandle handle) {
        checkPhase(BattleStatePhase.SETUP, true);
        this.handle = handle;
        phase = BattleStatePhase.INITIALIZATION;
    }

    @Override
    public void ready() {
        checkPhase(BattleStatePhase.INITIALIZATION, true);
        phase = BattleStatePhase.FIGHT;
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
    public <View extends BattleEffect> View getEffectView(final BattleEffectType<View, ?> type) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        final BattleEffect effect = effects.get(type);
        if (effect == null) {
            throw new TBCExException();
        }
        return (View) effect;
    }

    @Override
    public BattleBounds getBattleBounds() {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        return bounds;
    }

    @Override
    public Iterable<BattleParticipantHandle> getParticipants() {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        return Iterables.unmodifiableIterable(participantStates.keySet());
    }

    @Override
    public <View extends BattleEffect, Effect extends View> Effect getEffect(final BattleEffectType<View, Effect> type) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        final BattleEffect effect = effects.get(type);
        if (effect == null) {
            throw new TBCExException();
        }
        return (Effect) effect;
    }

    @Override
    public void removeEffect(final BattleEffectType<?, ?> type, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        final BattleEffect removed = effects.remove(type);
        if (removed == null) {
            throw new TBCExException();
        }
        removed.deinit();
    }

    @Override
    public void addEffect(final BattleEffect effect, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        if (effects.put(effect.getType(), effect) != null) {
            throw new TBCExException();
        }
        effect.init(this, tracer);
    }

    @Override
    public boolean setBattleBounds(final BattleBounds bounds, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        if (events.getEvent(CoreBattleEvents.PRE_BATTLE_BOUNDS_SET_EVENT).getInvoker().preBattleBoundsSet(this, bounds, tracer)) {
            final BattleBounds old = this.bounds;
            this.bounds = bounds;
            events.getEvent(CoreBattleEvents.POST_BATTLE_BOUNDS_SET_EVENT).getInvoker().postBattleBoundsSet(this, old, tracer);
            return true;
        }
        return false;
    }

    @Override
    public BattleParticipantState getParticipant(final BattleParticipantHandle handle) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        final BattleParticipantState state = participantStates.get(handle);
        if (state == null) {
            throw new TBCExException();
        }
        return state;
    }

    @Override
    public BattleHandle getHandle() {
        return handle;
    }

    @Override
    public boolean addParticipant(final BattleParticipantState participant, final Optional<BattleParticipantHandle> invitation, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleStatePhase.INITIALIZATION, false);
        if (participant.getPhase().getOrder() > BattleParticipantStatePhase.SETUP.getOrder()) {
            throw new TBCExException();
        }
        if (!(participant instanceof AbstractBattleParticipantState)) {
            throw new TBCExException();
        }
        final BattleParticipantHandle handle = BattleParticipantHandle.of(participant.getUuid(), this.handle);
        if (participantStates.containsKey(handle)) {
            throw new TBCExException();
        }
        if (invitation.isEmpty()) {
            if (phase.getOrder() > BattleStatePhase.INITIALIZATION.getOrder()) {
                throw new TBCExException();
            }
        }
        if (events.getEvent(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_JOIN_EVENT).getInvoker().preBattleParticipantJoin(participant, tracer)) {
            participantStates.put(handle, (AbstractBattleParticipantState) participant);
            events.getEvent(CoreBattleEvents.POST_BATTLE_PARTICIPANT_JOIN_EVENT).getInvoker().postBattleParticipantJoin(participant, tracer);
            return true;
        }
        return false;
    }

    private void checkPhase(final BattleStatePhase phase, final boolean exact) {
        if ((exact && phase != this.phase) || (!exact && this.phase.getOrder() < phase.getOrder())) {
            throw new TBCExException();
        }
    }
}
