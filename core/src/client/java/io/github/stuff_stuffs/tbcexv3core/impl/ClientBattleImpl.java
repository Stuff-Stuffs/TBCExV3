package io.github.stuff_stuffs.tbcexv3core.impl;

import io.github.stuff_stuffs.tbcexv3core.api.animation.ActionTraceAnimatorRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.animation.BattleAnimationContext;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerEventStream;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.AbstractBattleStateImpl;
import io.github.stuff_stuffs.tbcexv3core.impl.battles.ClientBattleEnvironmentImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.github.stuff_stuffs.tbcexv3core.internal.common.environment.BattleEnvironmentSection;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.function.Consumer;

public class ClientBattleImpl implements Battle {
    private static final ActionTrace ROOT_START_TRACER = ActionTrace.BattleStart.INSTANCE;
    private static final ActionTrace ROOT_END_TRACER = ActionTrace.BattleEnd.INSTANCE;
    private final ObjectArrayList<BattleAction> actions;
    private final BattleHandle handle;
    private final BattleStateMode mode;
    private final BattleEnvironmentImpl.Initial initialEnvironment;
    private final BlockPos origin;
    private final TracerEventStream<ActionTrace> eventStream;
    private final Consumer<Consumer<AnimationManager<BattleAnimationContext>>> animationConsumer;
    private ClientBattleEnvironmentImpl environment;
    private AbstractBattleStateImpl state;
    private Tracer<ActionTrace> tracer;
    private int lastKnownGoodState;

    public ClientBattleImpl(final BattleHandle handle, final BattleStateMode mode, final BattleEnvironmentImpl.Initial environment, final BlockPos origin, final Consumer<Consumer<AnimationManager<BattleAnimationContext>>> consumer) {
        animationConsumer = consumer;
        eventStream = TracerEventStream.create();
        this.handle = handle;
        this.mode = mode;
        initialEnvironment = environment;
        this.origin = origin;
        ClientBattleEnvironmentImpl env = createBattleEnvironment();
        this.environment = env;
        state = (AbstractBattleStateImpl) BattleState.createEmpty(this.mode);
        state.setup(handle, this.environment);
        env.setup(state);
        actions = new ObjectArrayList<>();
        tracer = createTracer();
    }

    private ClientBattleEnvironmentImpl createBattleEnvironment() {
        final BattleEnvironmentSection[] sections = new BattleEnvironmentSection[initialEnvironment.sections().size()];
        for (int i = 0; i < sections.length; i++) {
            sections[i] = initialEnvironment.sections().get(i).create();
        }
        return new ClientBattleEnvironmentImpl(initialEnvironment.outOfBoundsState(), initialEnvironment.outOfBoundsBiome(), initialEnvironment.min(), initialEnvironment.max(), sections);
    }

    private Tracer<ActionTrace> createTracer() {
        return Tracer.create(ROOT_START_TRACER, ROOT_END_TRACER);
    }

    public void update(final BattleUpdate update) {
        if (getActionCount() + 1 < update.offset()) {
            throw new IllegalStateException("Battle update advanced past known position!");
        }
        trimActions(update.offset());
        for (final BattleAction action : update.actions()) {
            pushAction(action);
        }
        eventStream.update(tracer);
        eventStream.newEvents().map(ActionTraceAnimatorRegistry.INSTANCE::animate).filter(Optional::isPresent).map(Optional::get).forEach(animationConsumer);
        lastKnownGoodState = update.offset() + update.actions().size() - 1;
    }

    public BattleUpdateRequest createUpdateRequest() {
        return new BattleUpdateRequest(getState().getHandle(), lastKnownGoodState);
    }

    @Override
    public BlockPos origin() {
        return origin;
    }

    @Override
    public BattleState getState() {
        return state;
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

    @Override
    public BlockPos toLocal(final BlockPos global) {
        return global.subtract(origin).add(environment.min());
    }

    @Override
    public BlockPos toGlobal(final BlockPos local) {
        return local.subtract(environment.min()).add(origin);
    }

    @Override
    public Vec3d toLocal(final Vec3d global) {
        return global.subtract(origin.getX(), origin.getY(), origin.getZ()).add(environment.min().getX(), environment.min().getY(), environment.min().getZ());
    }

    @Override
    public Vec3d toGlobal(final Vec3d local) {
        return local.subtract(environment.min().getX(), environment.min().getY(), environment.min().getZ()).add(origin.getX(), origin.getY(), origin.getZ());
    }

    @Override
    public void trimActions(final int size) {
        if (size < actions.size()) {
            actions.removeElements(size, actions.size());
            tracer = createTracer();
            state = (AbstractBattleStateImpl) BattleState.createEmpty(mode);
            final ClientBattleEnvironmentImpl environment = createBattleEnvironment();
            this.environment = environment;
            state.setup(handle, environment);
            environment.setup(state);
            for (final BattleAction action : actions) {
                action.apply(state, tracer);
            }
        }
    }

    @Override
    public void pushAction(final BattleAction action) {
        if (mode == BattleStateMode.SERVER && action.getActor().isPresent()) {
            if (!state.isCurrentTurn(action.getActor().get())) {
                TBCExV3Core.getLogger().error("Tried to push an action out of turn, cancelling the action!");
                return;
            }
        }
        action.apply(state, tracer);
        actions.push(action);
    }

    @Override
    public boolean tryPushAction(final BattleAction action) {
        final Optional<BattleParticipantHandle> actor = action.getActor();
        if (actor.isEmpty() || !state.isCurrentTurn(actor.get())) {
            return false;
        }
        pushAction(action);
        return true;
    }
}
