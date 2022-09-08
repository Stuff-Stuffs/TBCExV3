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
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntityComponentMap;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.AbstractBattleParticipantInventory;
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
    private BattleState battleState;
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
        checkPhase(BattleParticipantStatePhase.SETUP, false);
        return events;
    }

    @Override
    public <View extends BattleParticipantEffect> View getEffectView(final BattleParticipantEffectType<View, ?> type) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, false);
        final BattleParticipantEffect effect = effects.get(type);
        if (effect == null) {
            throw new TBCExException();
        }
        return (View) effect;
    }

    @Override
    public <View extends BattleParticipantEffect, Effect extends View> Effect getEffect(final BattleParticipantEffectType<View, Effect> type) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, false);
        final BattleParticipantEffect effect = effects.get(type);
        if (effect == null) {
            throw new TBCExException();
        }
        return (Effect) effect;
    }

    @Override
    public void removeEffect(final BattleParticipantEffectType<?, ?> type, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, false);
        final BattleParticipantEffect removed = effects.remove(type);
        if (removed == null) {
            throw new TBCExException();
        }
        removed.deinit();
    }

    @Override
    public void addEffect(final BattleParticipantEffect effect, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, false);
        if (effects.put(effect.getType(), effect) != null) {
            throw new TBCExException();
        }
        effect.init(this, tracer);
    }

    @Override
    public BattleParticipantInventory getInventory() {
        return inventory;
    }

    @Override
    public BattleParticipantStatMap getStatMap() {
        return statMap;
    }

    @Override
    public BattleState getBattleState() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, false);
        return battleState;
    }

    @Override
    public UUID getUuid() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, false);
        return uuid;
    }

    @Override
    public BattleParticipantStatePhase getPhase() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, false);
        return phase;
    }

    @Override
    public BattleParticipantHandle getHandle() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, false);
        return handle;
    }

    @Override
    public <T extends BattleEntityComponent> Optional<T> getEntityComponent(final BattleEntityComponentType<T> componentType) {
        return componentMap.get(componentType);
    }

    @Override
    public void setup(final BattleState state) {
        checkPhase(BattleParticipantStatePhase.SETUP, true);
        battleState = state;
        handle = BattleParticipantHandle.of(getUuid(), state.getHandle());
        inventory.setup(this, handle);
        phase = BattleParticipantStatePhase.INITIALIZATION;
    }

    private void checkPhase(final BattleParticipantStatePhase phase, final boolean exact) {
        if ((exact && phase != this.phase) || (!exact && this.phase.getOrder() < phase.getOrder())) {
            throw new TBCExException();
        }
    }
}
