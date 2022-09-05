package io.github.stuff_stuffs.tbcexv3core.impl.battle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.util.CodecUtil;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.function.Function;

public class BattleImpl implements Battle, BattleView {
    public static final Encoder<Battle> CASTED_ENCODER = CodecUtil.castedCodec(Codec.list(BattleAction.CODEC).xmap(null, impl -> impl.actions), BattleImpl.class);
    public static final Decoder<Function<BattleStateMode, Battle>> CASTED_DECODER = Codec.list(BattleAction.CODEC).xmap(l -> mode -> new BattleImpl(l, mode), null);
    private static final ActionTrace ROOT_VALUE = ActionTrace.INSTANCE;
    private final ObjectArrayList<BattleAction> actions;
    private final BattleStateMode mode;
    private BattleState state;
    private Tracer<ActionTrace> tracer;

    public BattleImpl(final BattleStateMode mode) {
        this.mode = mode;
        BattleState.createEmpty(this.mode);
        actions = new ObjectArrayList<>();
        tracer = createTracer();
    }

    private Tracer<ActionTrace> createTracer() {
        return Tracer.create(ROOT_VALUE);
    }

    private BattleImpl(final List<BattleAction> actions, final BattleStateMode mode) {
        this(mode);
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
            state = BattleState.createEmpty(mode);
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
}
