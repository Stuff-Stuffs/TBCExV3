package io.github.stuff_stuffs.tbcexv3core.impl.battle;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.stuff_stuffs.tbcexv3core.api.battle.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battle.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battle.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class BattleImpl implements Battle {
    public static final Codec<BattleImpl> CODEC = Codec.list(BattleAction.CODEC).xmap(BattleImpl::new, impl -> impl.actions);
    private final ObjectArrayList<BattleAction> actions;
    private BattleState state = BattleState.createEmpty();
    private Tracer<ActionTrace> tracer;

    public BattleImpl() {
        actions = new ObjectArrayList<>();
        tracer = createTracer();
    }

    private Tracer<ActionTrace> createTracer() {
        return Tracer.create(ActionTrace.INSTANCE);
    }

    private BattleImpl(final List<BattleAction> actions) {
        this();
        for (final BattleAction action : actions) {
            pushAction(action);
        }
    }

    @Override
    public BattleState getState() {
        return state;
    }

    @Override
    public void trimActions(final int size) {
        if (size < actions.size()) {
            actions.removeElements(size, actions.size());
            tracer = createTracer();
            state = BattleState.createEmpty();
            for (final BattleAction action : actions) {
                action.apply(state, tracer);
            }
        }
    }

    @Override
    public void pushAction(final BattleAction action) {
        action.apply(state, tracer);
        actions.push(action);
    }

    @Override
    public int getActionCount() {
        return actions.size();
    }

    @Override
    public BattleAction getAction(final int index) {
        return actions.get(index);
    }

    static <K> DataResult<Battle> decode(final DynamicOps<K> ops, final K battle) {
        return CODEC.decode(ops, battle).map(Pair::getFirst);
    }
}
