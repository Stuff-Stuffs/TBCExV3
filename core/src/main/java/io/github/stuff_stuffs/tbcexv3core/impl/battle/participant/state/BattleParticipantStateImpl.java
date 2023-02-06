package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.state;

import com.google.common.collect.Iterators;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ParticipantActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.CoreBattleParticipantEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventory;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatMap;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.CoreBattleParticipantStats;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentMap;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.AbstractBattleParticipantInventory;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.AbstractBattleStateImpl;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;
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
    private BattleParticipantBounds bounds;
    private double health = 1.0;

    public BattleParticipantStateImpl(final UUID uuid, final BattleEntityComponentMap componentMap, final BattleParticipantBounds bounds) {
        this.componentMap = componentMap;
        final EventMap.Builder builder = EventMap.Builder.create();
        BattleParticipantState.BATTLE_PARTICIPANT_EVENT_INITIALIZATION_EVENT.invoker().addEvents(builder);
        events = builder.build();
        this.uuid = uuid;
        effects = new Object2ReferenceOpenHashMap<>();
        inventory = AbstractBattleParticipantInventory.createBlank();
        statMap = BattleParticipantStatMap.create();
        phase = BattleParticipantStatePhase.SETUP;
        this.bounds = bounds;
    }

    @Override
    public EventMap getEventMap() {
        checkPhase(BattleParticipantStatePhase.SETUP, BattleParticipantStatePhase.FIGHT);
        return events;
    }

    @Override
    public Iterator<BattleParticipantEffectType<?, ?>> getEffects() {
        return Iterators.unmodifiableIterator(effects.keySet().iterator());
    }

    @Override
    public boolean setPosition(final BlockPos pos, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleParticipantStatePhase.FIGHT, BattleParticipantStatePhase.FIGHT);
        if (events.getEvent(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT).getInvoker().preSetBounds(this, bounds, tracer)) {
            final BattleParticipantBounds oldBounds = bounds;
            bounds = BattleParticipantBounds.move(pos, bounds);
            tracer.pushInstant(true).value(new ParticipantActionTraces.BattleParticipantMove(handle, oldBounds.center(), pos)).buildAndApply();
            events.getEvent(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT).getInvoker().preSetBounds(this, oldBounds, tracer);
            tracer.pop();
            return true;
        }
        return false;
    }

    @Override
    public boolean setBounds(final BattleParticipantBounds bounds, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FIGHT);
        if (events.getEvent(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT).getInvoker().preSetBounds(this, bounds, tracer)) {
            final BattleParticipantBounds oldBounds = this.bounds;
            this.bounds = bounds;
            tracer.pushInstant(true).value(new ParticipantActionTraces.BattleParticipantChangeBounds(handle, oldBounds, bounds)).buildAndApply();
            events.getEvent(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT).getInvoker().preSetBounds(this, oldBounds, tracer);
            tracer.pop();
            return true;
        }
        return false;
    }

    @Override
    public <View extends BattleParticipantEffect> Optional<View> getEffectView(final BattleParticipantEffectType<View, ?> type) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        final BattleParticipantEffect effect = effects.get(type);
        return Optional.ofNullable((View) effect);
    }

    @Override
    public BattleParticipantBounds getBounds() {
        return bounds;
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
        tracer.pushInstant(true).value(new ParticipantActionTraces.BattleParticipantRemoveEffect(handle, type)).buildAndApply();
        removed.deinit(tracer);
        tracer.pop();
    }

    @Override
    public void addEffect(BattleParticipantEffect effect, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FIGHT);
        final BattleParticipantEffect current = effects.put(effect.getType(), effect);
        if (current != null) {
            effect = combine(current, effect, effect.getType());
            effects.put(effect.getType(), effect);
        }
        tracer.pushInstant(true).value(new ParticipantActionTraces.BattleParticipantAddEffect(handle, current != null, effect.getType())).buildAndApply();
        effect.init(this, tracer);
        tracer.pop();
    }

    private static <K extends BattleParticipantEffect, T extends K> BattleParticipantEffect combine(final BattleParticipantEffect currentEffect, final BattleParticipantEffect newEffect, final BattleParticipantEffectType<K, T> type) {
        final T currentCasted = type.checkedCast(currentEffect);
        final T newCasted = type.checkedCast(newEffect);
        if (currentCasted == null || newCasted == null) {
            throw new RuntimeException();
        }
        return type.combine(currentCasted, newCasted);
    }

    @Override
    public boolean setTeam(final BattleParticipantTeam team, final Tracer<ActionTrace> tracer) {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return battleState.setTeam(handle, team, tracer);
    }

    @Override
    public double setHealth(final double amount, final Tracer<ActionTrace> tracer) {
        final double newHealth = events.getEvent(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_HEALTH_EVENT).getInvoker().preSetHealth(this, amount, tracer);
        final double maxHealth = statMap.compute(CoreBattleParticipantStats.MAX_HEALTH, null);
        final double realHealth = Math.min(newHealth, maxHealth);
        if (newHealth != health) {
            final double oldHealth = health;
            tracer.pushInstant(true).value(
                    realHealth < oldHealth ?
                            new ParticipantActionTraces.Health.Damage(handle, oldHealth, oldHealth - realHealth) :
                            new ParticipantActionTraces.Health.Heal(handle, oldHealth, realHealth - oldHealth)
            ).buildAndApply();
            events.getEvent(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_HEALTH_EVENT).getInvoker().postSetHealth(this, oldHealth, tracer);
            health = realHealth;
            if (realHealth <= 0) {
                events.getEvent(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_DEATH_EVENT).getInvoker().preDeath(this, tracer);
                if (health <= 0) {
                    if (battleState.removeParticipant(handle, BattleParticipantRemovalReason.DIED, tracer)) {
                        return 0;
                    } else if (health <= 0) {
                        throw new TBCExException("Participant with zero health unable to leave battle!");
                    }
                }
            }
            tracer.pop();
            return health;
        }
        return health;
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
        return uuid;
    }

    @Override
    public BattleParticipantStatePhase getPhase() {
        return phase;
    }

    @Override
    public BattleParticipantHandle getHandle() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return handle;
    }

    @Override
    public BattleParticipantTeam getTeam() {
        checkPhase(BattleParticipantStatePhase.INITIALIZATION, BattleParticipantStatePhase.FINISHED);
        return battleState.getTeamByParticipant(handle);
    }

    @Override
    public double getHealth() {
        return health;
    }

    @Override
    public <T extends BattleEntityComponent> Optional<T> getEntityComponent(final BattleEntityComponentType<T> componentType) {
        return componentMap.get(componentType);
    }

    @Override
    public Iterator<? extends BattleEntityComponent> entityComponents() {
        return componentMap.components();
    }

    @Override
    public void setup(final AbstractBattleStateImpl state) {
        checkPhaseExact(BattleParticipantStatePhase.SETUP);
        battleState = state;
        handle = BattleParticipantHandle.of(uuid, state.getHandle());
        inventory.setup(this, handle);
        phase = BattleParticipantStatePhase.INITIALIZATION;
    }

    @Override
    public void finish() {
        phase = BattleParticipantStatePhase.FINISHED;
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
