package io.github.stuff_stuffs.tbcexv3core.impl.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battle.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battle.effect.BattleEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.util.Map;

public class BattleStateImpl implements BattleState {
    private final EventMap events;
    private final Map<BattleEffectType<?, ?>, BattleEffect> effects;

    public BattleStateImpl() {
        final EventMap.Builder builder = EventMap.Builder.create();
        BattleState.BATTLE_EVENT_INITIALIZATION_EVENT.invoker().addEvents(builder);
        events = builder.build();
        effects = new Reference2ReferenceOpenHashMap<>();
    }

    @Override
    public EventMap getEventMap() {
        return events;
    }

    @Override
    public boolean hasEffect(final BattleEffectType<?, ?> type) {
        return effects.containsKey(type);
    }

    @Override
    public <View extends BattleEffect> View getEffectView(final BattleEffectType<View, ?> type) {
        final BattleEffect effect = effects.get(type);
        if (effect == null) {
            throw new TBCExException();
        }
        return (View) effect;
    }

    @Override
    public <View extends BattleEffect, Effect extends View> Effect getEffect(final BattleEffectType<View, Effect> type) {
        final BattleEffect effect = effects.get(type);
        if (effect == null) {
            throw new TBCExException();
        }
        return (Effect) effect;
    }

    @Override
    public void removeEffect(final BattleEffectType<?, ?> type, final Tracer<ActionTrace> tracer) {
        final BattleEffect removed = effects.remove(type);
        if (removed == null) {
            throw new TBCExException();
        }
        removed.deinit();
    }

    @Override
    public void addEffect(final BattleEffect effect, final Tracer<ActionTrace> tracer) {
        if (effects.put(effect.getType(), effect) != null) {
            throw new TBCExException();
        }
        effect.init(this, tracer);
    }
}
