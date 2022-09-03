package io.github.stuff_stuffs.tbcexv3core.impl.battle;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.util.CodecUtil;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

public class BattleImpl implements Battle, BattleView {
    public static final Codec<BattleImpl> CODEC = Codec.list(BattleAction.CODEC).xmap(BattleImpl::new, impl -> impl.actions);
    public static final Codec<Battle> CASTED_CODEC = CodecUtil.castedCodec(CODEC, BattleImpl.class);
    private static final ActionTrace ROOT_VALUE = ActionTrace.INSTANCE;
    private final ObjectArrayList<BattleAction> actions;
    private BattleState state = BattleState.createEmpty();
    private Tracer<ActionTrace> tracer;

    public BattleImpl() {
        actions = new ObjectArrayList<>();
        tracer = createTracer();
    }

    private Tracer<ActionTrace> createTracer() {
        return Tracer.create(ROOT_VALUE);
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
        if (tracer.getCurrentStage().getValue() == ROOT_VALUE) {
            TBCExV3Core.LOGGER.error("Pushed action while tracer was not at root");
        }
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
