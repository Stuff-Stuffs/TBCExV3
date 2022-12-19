package io.github.stuff_stuffs.tbcexv3core.impl.battle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.AbstractBattleStateImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.function.BiFunction;

public class BattleImpl implements Battle, BattleView {
    public static final Encoder<Battle> CASTED_ENCODER = CodecUtil.castedCodec(Codec.list(BattleAction.CODEC).xmap(null, impl -> impl.actions), BattleImpl.class, Battle.class);
    public static final Decoder<BiFunction<BattleHandle, BattleStateMode, Battle>> CASTED_DECODER = Codec.list(BattleAction.CODEC).xmap(l -> (handle, mode) -> new BattleImpl(l, mode, handle), battle -> null);
    private static final ActionTrace ROOT_START_TRACER = ActionTrace.INSTANCE;
    private static final ActionTrace ROOT_END_TRACER = ActionTrace.INSTANCE;
    private final ObjectArrayList<BattleAction> actions;
    private final BattleHandle handle;
    private final BattleStateMode mode;
    private AbstractBattleStateImpl state;
    private Tracer<ActionTrace> tracer;

    public BattleImpl(final BattleHandle handle, final BattleStateMode mode) {
        this.handle = handle;
        this.mode = mode;
        state = (AbstractBattleStateImpl) BattleState.createEmpty(this.mode);
        state.setup(handle);
        actions = new ObjectArrayList<>();
        tracer = createTracer();
    }

    private Tracer<ActionTrace> createTracer() {
        return Tracer.create(ROOT_START_TRACER, ROOT_END_TRACER);
    }

    private BattleImpl(final List<BattleAction> actions, final BattleStateMode mode, final BattleHandle handle) {
        this(handle, mode);
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
            state = (AbstractBattleStateImpl) BattleState.createEmpty(mode);
            state.setup(handle);
            for (final BattleAction action : actions) {
                action.apply(state, tracer);
            }
        }
    }

    @Override
    public void pushAction(final BattleAction action) {
        if (mode == BattleStateMode.SERVER && action.getActor().isPresent()) {
            if (!state.isCurrentTurn(action.getActor().get())) {
                TBCExV3Core.LOGGER.error("Tried to push an action out of turn!");
                return;
            }
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

    @Override
    public TracerView<ActionTrace> tracer() {
        return tracer;
    }
}
