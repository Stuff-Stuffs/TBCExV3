package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.action.BattleParticipantActionBuilderImpl;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApiStatus.NonExtendable
public interface BattleParticipantActionBuilder<R> {
    <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(BattleParticipantActionTargetType<T> type);

    <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(BattleParticipantActionTargetType<T> type);

    Iterator<? extends BattleParticipantActionTargetType<?>> types();

    R renderData();

    boolean canBuild();

    void build();

    static <S> BattleParticipantActionBuilder<Unit> create(final BattleParticipantStateView view, final Predicate<S> predicate, final Function<S, ? extends BattleAction> builder, final S state, final TargetProviderFactory<S> factory, final BiConsumer<S, BattleParticipantActionTarget> updater, final Consumer<BattleAction> consumer) {
        return create(view, predicate, builder, state, factory, updater, consumer, Unit.INSTANCE, (p, s) -> p);
    }

    static <R, S> BattleParticipantActionBuilder<R> create(final BattleParticipantStateView view, final Predicate<S> predicate, final Function<S, ? extends BattleAction> builder, final S state, final TargetProviderFactory<S> factory, final BiConsumer<S, BattleParticipantActionTarget> updater, final Consumer<BattleAction> consumer, final R data, final BiFunction<R, S, R> renderDataUpdater) {
        return new BattleParticipantActionBuilderImpl<>(view, predicate, builder, state, factory, updater, consumer, data, renderDataUpdater);
    }

    record RaycastIterator<T extends BattleParticipantActionTarget>(
            TargetRaycaster<? extends T> raycaster,
            TargetIterator<? extends T> iterator
    ) {
    }

    interface TargetRaycaster<T extends BattleParticipantActionTarget> {
        default Optional<? extends Pair<? extends T, Double>> raycast(final Vector3d start, final Vector3d end) {
            return raycast(new Vec3d(start.x, start.y, start.z), new Vec3d(end.x, end.y, end.z));
        }

        Optional<? extends Pair<? extends T, Double>> raycast(Vec3d start, Vec3d end);

        default Optional<? extends Pair<? extends T, Double>> query(final Vector3d start, final Vector3d end) {
            return query(new Vec3d(start.x, start.y, start.z), new Vec3d(end.x, end.y, end.z));
        }

        Optional<? extends Pair<? extends T, Double>> query(Vec3d start, Vec3d end);

        static <T extends BattleParticipantActionTarget> TargetRaycaster<T> of(final Supplier<? extends Iterator<? extends T>> iteratorFactory, final Function<? super T, ? extends Iterator<? extends Box>> boxFactory, final Consumer<T> consumer) {
            return new TargetRaycaster<>() {
                @Override
                public Optional<? extends Pair<? extends T, Double>> raycast(final Vec3d start, final Vec3d end) {
                    final Optional<? extends Pair<? extends T, Double>> query = query(start, end);
                    if (query.isEmpty()) {
                        return Optional.empty();
                    }
                    final Iterator<? extends T> iterator = iteratorFactory.get();
                    while (iterator.hasNext()) {
                        final T next = iterator.next();
                        final Iterator<? extends Box> boxIter = boxFactory.apply(next);
                        while (boxIter.hasNext()) {
                            final Optional<Vec3d> raycast = boxIter.next().raycast(start, end);
                            if (raycast.isPresent()) {
                                final double dist = raycast.get().squaredDistanceTo(start);
                                if (dist == query.get().getSecond()) {
                                    consumer.accept(next);
                                    return query;
                                }
                            }
                        }
                    }
                    return Optional.empty();
                }

                @Override
                public Optional<? extends Pair<? extends T, Double>> query(final Vec3d start, final Vec3d end) {
                    final Iterator<? extends T> iterator = iteratorFactory.get();
                    double bestDist = Double.POSITIVE_INFINITY;
                    T best = null;
                    while (iterator.hasNext()) {
                        final T next = iterator.next();
                        final Iterator<? extends Box> boxIter = boxFactory.apply(next);
                        while (boxIter.hasNext()) {
                            final Optional<Vec3d> raycast = boxIter.next().raycast(start, end);
                            if (raycast.isPresent()) {
                                final double dist = raycast.get().squaredDistanceTo(start);
                                if (dist < bestDist) {
                                    best = next;
                                    bestDist = dist;
                                }
                            }
                        }
                    }
                    return best == null ? Optional.empty() : Optional.of(Pair.of(best, bestDist));
                }
            };
        }


        static <T extends BattleParticipantActionTarget> TargetRaycaster<T> empty() {
            return new TargetRaycaster<>() {
                @Override
                public Optional<Pair<? extends T, Double>> raycast(final Vec3d start, final Vec3d end) {
                    return Optional.empty();
                }

                @Override
                public Optional<? extends Pair<? extends T, Double>> query(final Vec3d start, final Vec3d end) {
                    return Optional.empty();
                }
            };
        }

        static <T extends BattleParticipantActionTarget> TargetRaycaster<T> union(final TargetRaycaster<? extends T>[] raycasters) {
            return new TargetRaycaster<>() {
                @Override
                public Optional<? extends Pair<? extends T, Double>> raycast(final Vec3d start, final Vec3d end) {
                    Optional<? extends Pair<? extends T, Double>> opt = Optional.empty();
                    int bestIndex = -1;
                    for (int i = 0; i < raycasters.length; i++) {
                        final TargetRaycaster<? extends T> raycaster = raycasters[i];
                        final Optional<? extends Pair<? extends T, Double>> raycast = raycaster.query(start, end);
                        if (raycast.isPresent() && (opt.isEmpty() || raycast.get().getSecond() < opt.get().getSecond())) {
                            opt = raycast;
                            bestIndex = i;
                        }
                    }
                    if (bestIndex == -1) {
                        return Optional.empty();
                    } else {
                        return raycasters[bestIndex].raycast(start, end);
                    }
                }

                @Override
                public Optional<? extends Pair<? extends T, Double>> query(final Vec3d start, final Vec3d end) {
                    Optional<? extends Pair<? extends T, Double>> opt = Optional.empty();
                    for (final TargetRaycaster<? extends T> raycaster : raycasters) {
                        final Optional<? extends Pair<? extends T, Double>> raycast = raycaster.query(start, end);
                        if (raycast.isPresent() && (opt.isEmpty() || raycast.get().getSecond() < opt.get().getSecond())) {
                            opt = raycast;
                        }
                    }
                    return opt;
                }
            };
        }
    }

    interface TargetIterator<T extends BattleParticipantActionTarget> {
        boolean hasNext();

        T next();

        void accept();

        static <T extends BattleParticipantActionTarget> TargetIterator<T> empty() {
            return new TargetIterator<>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public T next() {
                    throw new IllegalStateException();
                }

                @Override
                public void accept() {
                    throw new IllegalStateException();
                }
            };
        }

        static <T extends BattleParticipantActionTarget> TargetIterator<T> of(final Iterator<T> iterator, final Consumer<? super T> consumer) {
            return new TargetIterator<>() {
                private T last;
                private boolean finished = false;

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public T next() {
                    return last = iterator.next();
                }

                @Override
                public void accept() {
                    if (finished) {
                        throw new IllegalStateException("Tried to accept target twice!");
                    }
                    if (last == null) {
                        throw new IllegalStateException("Tried to accept target before advancing!");
                    }
                    finished = true;
                    consumer.accept(last);
                }
            };
        }

        @SafeVarargs
        static <T extends BattleParticipantActionTarget> TargetIterator<T> union(final TargetIterator<? extends T>... iterators) {
            return new TargetIterator<>() {
                private int index = 0;
                private TargetIterator<? extends T> iterator = iterators[0];

                @Override
                public boolean hasNext() {
                    while (index + 1 < iterators.length && !iterator.hasNext()) {
                        index++;
                        iterator = iterators[index];
                    }
                    return index + 1 < iterators.length;
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new IllegalStateException();
                    }
                    return iterator.next();
                }

                @Override
                public void accept() {
                    iterator.accept();
                }
            };
        }
    }

    interface TargetProvider {
        <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(BattleParticipantActionTargetType<T> type);

        <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(BattleParticipantActionTargetType<T> type);

        Iterator<? extends BattleParticipantActionTargetType<?>> types();

        static TargetProvider empty() {
            return new TargetProvider() {
                @Override
                public <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(final BattleParticipantActionTargetType<T> type) {
                    return TargetIterator.empty();
                }

                @Override
                public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type) {
                    return TargetRaycaster.empty();
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return Collections.emptyIterator();
                }
            };
        }

        static <T extends BattleParticipantActionTarget> TargetProvider single(final BattleParticipantStateView stateView, final Consumer<BattleParticipantActionTarget> targetConsumer, final BattleParticipantActionTargetType<T> type, final Supplier<? extends TargetIterator<? extends T>> iteratorFactory, final Supplier<? extends TargetRaycaster<? extends T>> raycastFactory) {
            return new TargetProvider() {
                @Override
                public <T0 extends BattleParticipantActionTarget> TargetIterator<? extends T0> targets(final BattleParticipantActionTargetType<T0> t) {
                    if (t == type) {
                        return (TargetIterator<? extends T0>) iteratorFactory.get();
                    }
                    return TargetIterator.empty();
                }

                @Override
                public <T0 extends BattleParticipantActionTarget> TargetRaycaster<? extends T0> raycastTargets(final BattleParticipantActionTargetType<T0> t) {
                    if (t == type) {
                        return (TargetRaycaster<? extends T0>) raycastFactory.get();
                    }
                    return TargetRaycaster.empty();
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return Iterators.singletonIterator(type);
                }
            };
        }

        static TargetProvider typeCheck(final TargetProvider delegate, final BattleParticipantActionTargetType<?> type) {
            return typeCheck(delegate, i -> i == type, () -> Iterators.singletonIterator(type));
        }

        static TargetProvider typeCheck(final TargetProvider delegate, final Predicate<BattleParticipantActionTargetType<?>> typeChecker, final Supplier<Iterator<? extends BattleParticipantActionTargetType<?>>> typeIterator) {
            return new TargetProvider() {
                @Override
                public <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(final BattleParticipantActionTargetType<T> type) {
                    if (typeChecker.test(type)) {
                        return delegate.targets(type);
                    }
                    return TargetIterator.empty();
                }

                @Override
                public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type) {
                    if (typeChecker.test(type)) {
                        return delegate.raycastTargets(type);
                    }
                    return TargetRaycaster.empty();
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return typeIterator.get();
                }
            };
        }

        static TargetProvider union(final TargetProvider... providers) {
            if (providers.length == 0) {
                return empty();
            }
            if (providers.length == 1) {
                return providers[0];
            }
            final Set<? extends BattleParticipantActionTargetType<?>> collect = Arrays.stream(providers).flatMap(provider -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(provider.types(), 0), false)).collect(Collectors.toUnmodifiableSet());
            return new TargetProvider() {
                @Override
                public <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(final BattleParticipantActionTargetType<T> type) {
                    final TargetIterator<? extends T>[] iterators = new TargetIterator[providers.length];
                    for (int i = 0; i < providers.length; i++) {
                        iterators[i] = providers[i].targets(type);
                    }
                    return TargetIterator.union(iterators);
                }

                @Override
                public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type) {
                    final TargetRaycaster<? extends T>[] raycasters = new TargetRaycaster[providers.length];
                    for (int i = 0; i < providers.length; i++) {
                        raycasters[i] = providers[i].raycastTargets(type);
                    }
                    return TargetRaycaster.union(raycasters);
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return collect.iterator();
                }
            };
        }
    }

    interface TargetProviderFactory<T> {
        TargetProvider build(BattleParticipantStateView stateView, T state, Consumer<BattleParticipantActionTarget> targetConsumer);

        @SafeVarargs
        static <S> TargetProviderFactory<S> union(final TargetProviderFactory<S>... factories) {
            return (stateView, state, targetConsumer) -> {
                final TargetProvider[] providers = new TargetProvider[factories.length];
                for (int i = 0; i < factories.length; i++) {
                    providers[i] = factories[i].build(stateView, state, targetConsumer);
                }
                return TargetProvider.union(providers);
            };
        }

        static <T> TargetProviderFactory<T> orEmpty(final TargetProviderFactory<T> delegate, final Predicate<T> predicate) {
            return (stateView, state, targetConsumer) -> predicate.test(state) ? delegate.build(stateView, state, targetConsumer) : TargetProvider.empty();
        }

        static <T> TargetProviderFactory<T> orElse(final TargetProviderFactory<T> delegate, final Predicate<T> predicate, final TargetProviderFactory<T> fallback) {
            return (stateView, state, targetConsumer) -> predicate.test(state) ? delegate.build(stateView, state, targetConsumer) : fallback.build(stateView, state, targetConsumer);
        }
    }
}
