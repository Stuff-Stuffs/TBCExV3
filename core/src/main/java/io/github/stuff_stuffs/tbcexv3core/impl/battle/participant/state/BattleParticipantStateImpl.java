package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventory;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatMap;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentMap;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.AbstractBattleParticipantInventory;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.AbstractBattleStateImpl;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BattleParticipantStateImpl implements AbstractBattleParticipantState {
    private final EventMap events;
    private final UUID uuid;
    private final Map<BattleParticipantEffectType<?, ?>, BattleParticipantEffect> effects;
    private final AbstractBattleParticipantInventory inventory;
    private final BattleParticipantStatMap statMap;
    private final BattleEntityComponentMap componentMap;
    private BattleParticipantHandle handle;
    private AbstractBattleStateImpl battleState;
    private BattleParticipantStatePhase phase;

    public BattleParticipantStateImpl(final UUID uuid, final BattleEntityComponentMap componentMap) {
        this.componentMap = componentMap;
        final EventMap.Builder builder = EventMap.Builder.create();
        BattleParticipantState.BATTLE_PARTICIPANT_EVENT_INITIALIZATION_EVENT.invoker().addEvents(builder);
        events = builder.build();
        this.uuid = uuid;
        effects = new Reference2ObjectOpenHashMap<>();
        inventory = AbstractBattleParticipantInventory.createBlank();
        statMap = BattleParticipantStatMap.create();
        phase = BattleParticipantStatePhase.SETUP;
    }

    @Override
    public EventMap getEventMap() {
        checkPhase(BattleParticipantStatePhase.SETUP, BattleParticipantStatePhase.FIGHT);
        return events;
    }

    @Override
    public <View extends BattleParticipantEffect> Optional<View> getEffectView(final BattleParticipantEffectType<View, ?> type) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        final BattleParticipantEffect effect = effects.get(type);
        return Optional.ofNullable((View) effect);
    }

    @Override
    public <View extends BattleParticipantEffect, Effect extends View> Optional<Effect> getEffect(final BattleParticipantEffectType<View, Effect> type) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        final BattleParticipantEffect effect = effects.get(type);
        return Optional.ofNullable((Effect) effect);
    }

    @Override
    public void removeEffect(final BattleParticipantEffectType<?, ?> type, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FIGHT);
        final BattleParticipantEffect removed = effects.remove(type);
        if (removed == null) {
            throw new TBCExException();
        }
        removed.deinit();
    }

    @Override
    public void addEffect(final BattleParticipantEffect effect, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FIGHT);
        if (effects.put(effect.getType(), effect) != null) {
            throw new TBCExException();
        }
        effect.init(this, tracer);
    }

    @Override
    public BattleParticipantInventory getInventory() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return inventory;
    }

    @Override
    public BattleParticipantStatMap getStatMap() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return statMap;
    }

    @Override
    public BattleState getBattleState() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return battleState;
    }

    @Override
    public UUID getUuid() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return uuid;
    }

    @Override
    public BattleParticipantStatePhase getPhase() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return phase;
    }

    @Override
    public BattleParticipantHandle getHandle() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return handle;
    }

    @Override
    public <T extends BattleEntityComponent> Optional<T> getEntityComponent(final BattleEntityComponentType<T> componentType) {
        return componentMap.get(componentType);
    }

    @Override
    public void setup(final AbstractBattleStateImpl state) {
        checkPhaseExact(BattleParticipantStatePhase.SETUP);
        battleState = state;
        handle = BattleParticipantHandle.of(uuid, state.getHandle());
        inventory.setup(this, handle);
        phase = BattleParticipantStatePhase.INITIALIZATION;
    }

    private void checkPhaseExact(final BattleParticipantStatePhase phase) {
        if (phase != this.phase) {
            throw new TBCExException();
        }
    }

    private void checkPhase(final BattleParticipantStatePhase startInclusive, final BattleParticipantStatePhase endInclusive) {
        if (phase.getOrder() < startInclusive.getOrder() || phase.getOrder() > endInclusive.getOrder()) {
            throw new TBCExException();
        }
    }
}
