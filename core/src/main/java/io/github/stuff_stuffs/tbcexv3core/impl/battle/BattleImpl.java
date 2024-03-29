package io.github.stuff_stuffs.tbcexv3core.impl.battle;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironment;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.state.AbstractBattleStateImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Optional;

public class BattleImpl implements Battle, BattleView {
    private static final ActionTrace ROOT_START_TRACER = ActionTrace.BattleStart.INSTANCE;
    private static final ActionTrace ROOT_END_TRACER = ActionTrace.BattleEnd.INSTANCE;
    private final ObjectArrayList<BattleAction> actions;
    private final BattleHandle handle;
    private final BattleStateMode mode;
    private final BattleEnvironmentImpl.Initial initialEnvironment;
    private final World world;
    private final BlockPos origin;
    private final Runnable reset;
    private AbstractBattleStateImpl state;
    private Tracer<ActionTrace> tracer;

    public BattleImpl(final BattleHandle handle, final BattleStateMode mode, final BattleEnvironmentImpl.Initial environment, final World world, final BlockPos origin, final Runnable reset) {
        this.handle = handle;
        this.mode = mode;
        initialEnvironment = environment;
        this.reset = reset;
        state = (AbstractBattleStateImpl) BattleState.createEmpty(this.mode);
        final BattleEnvironment battleEnvironment = environment.create();
        reset.run();
        ((BattleEnvironmentImpl) battleEnvironment).setup(state);
        state.setup(handle, battleEnvironment);
        this.world = world;
        this.origin = origin;
        actions = new ObjectArrayList<>();
        tracer = createTracer();
    }

    private Tracer<ActionTrace> createTracer() {
        return Tracer.create(ROOT_START_TRACER, ROOT_END_TRACER);
    }

    private BattleImpl(final List<BattleAction> actions, final BattleStateMode mode, final BattleHandle handle, final BattleEnvironmentImpl.Initial environment, final World world, final BlockPos origin, final Runnable reset) {
        this(handle, mode, environment, world, origin, reset);
        for (final BattleAction action : actions) {
            pushAction(action);
        }
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
    public void trimActions(final int size) {
        if (size < actions.size()) {
            actions.removeElements(size, actions.size());
            tracer = createTracer();
            state = (AbstractBattleStateImpl) BattleState.createEmpty(mode);
            reset.run();
            final BattleEnvironment environment = initialEnvironment.create();
            ((BattleEnvironmentImpl) environment).setup(state);
            state.setup(handle, environment);
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
        return global.subtract(origin).add(environment().min());
    }

    @Override
    public BlockPos toGlobal(final BlockPos local) {
        return local.subtract(environment().min()).add(origin);
    }

    @Override
    public Vec3d toLocal(Vec3d global) {
        return global.subtract(origin.getX(), origin.getY(), origin.getZ()).add(environment().min().getX(), environment().min().getY(), environment().min().getZ());
    }

    @Override
    public Vec3d toGlobal(Vec3d local) {
        return local.subtract(environment().min().getX(), environment().min().getY(), environment().min().getZ()).add(origin.getX(), origin.getY(), origin.getZ());
    }

    public BattleEnvironmentImpl.Initial environment() {
        return initialEnvironment;
    }

    public static Encoder<BattleImpl> encoder(final Registry<Biome> registry) {
        return new Encoder<>() {
            @Override
            public <T> DataResult<T> encode(final BattleImpl input, final DynamicOps<T> ops, final T prefix) {
                return ops.mapBuilder().add(
                        "actions",
                        ops.createList(
                                input.actions.stream().map(action -> BattleAction.CODEC.encodeStart(ops, action).getOrThrow(false, s -> {
                                            throw new RuntimeException(s);
                                        })
                                )
                        )
                ).add(
                        "initialData",
                        BattleEnvironmentImpl.Initial.codec(registry).encodeStart(ops, input.initialEnvironment).getOrThrow(false, s -> {
                            throw new RuntimeException(s);
                        })
                ).build(prefix);
            }
        };
    }

    public static Decoder<Factory> decoder(final Registry<Biome> registry) {
        return new Decoder<>() {
            @Override
            public <T> DataResult<Pair<Factory, T>> decode(final DynamicOps<T> ops, final T input) {
                final MapLike<T> map = ops.getMap(input).getOrThrow(false, s -> {
                    throw new RuntimeException(s);
                });
                final List<BattleAction> actions = new ObjectArrayList<>();
                ops.getList(map.get("actions")).getOrThrow(false, s -> {
                    throw new RuntimeException(s);
                }).accept(encodedAction -> {
                    final DataResult<BattleAction> parsed = BattleAction.CODEC.parse(ops, encodedAction);
                    actions.add(parsed.getOrThrow(false, s -> {
                        throw new RuntimeException(s);
                    }));
                });
                final BattleEnvironmentImpl.Initial initial = BattleEnvironmentImpl.Initial.codec(registry).parse(ops, map.get("initialData")).getOrThrow(false, s -> {
                    throw new RuntimeException(s);
                });
                return DataResult.success(Pair.of(new Factory() {
                    @Override
                    public Battle create(final BattleHandle handle, final BattleStateMode mode, final World world, final BlockPos pos, final Runnable worldReset) {
                        return new BattleImpl(actions, mode, handle, initial, world, pos, worldReset);
                    }

                    @Override
                    public BattleEnvironmentImpl.Initial environment() {
                        return initial;
                    }
                }, ops.empty()));
            }
        };
    }
}
