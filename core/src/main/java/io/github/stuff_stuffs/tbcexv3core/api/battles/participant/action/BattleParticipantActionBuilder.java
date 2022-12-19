package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.action.BattleParticipantActionBuilderImpl;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApiStatus.NonExtendable
public interface BattleParticipantActionBuilder {
    <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(BattleParticipantActionTargetType<T> type);

    <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(BattleParticipantActionTargetType<T> type, Vec3d start, Vec3d end);

    Iterator<? extends BattleParticipantActionTargetType<?>> types();

    boolean canBuild();

    void build();

    static <S> BattleParticipantActionBuilder create(final BattleParticipantStateView view, final Predicate<S> predicate, final Function<S, BattleAction> builder, final S state, final TargetProviderFactory<S> factory, final BiConsumer<S, BattleParticipantActionTarget> updater, final Consumer<BattleAction> consumer) {
        return new BattleParticipantActionBuilderImpl<>(view, predicate, builder, state, factory, updater, consumer);
    }

    interface TargetRaycaster<T extends BattleParticipantActionTarget> {
        boolean valid();

        void accept();

        Optional<? extends Pair<? extends T, Double>> raycast();

        static <T extends BattleParticipantActionTarget> TargetRaycaster<T> of(final List<T> list, final Function<T, Iterator<Box>> boxExtractor, final Consumer<? super T> consumer, final BooleanSupplier valid, final Vec3d start, final Vec3d end) {
            return new TargetRaycaster<>() {
                private int lastIndex = -1;

                @Override
                public boolean valid() {
                    return valid.getAsBoolean();
                }

                @Override
                public void accept() {
                    if (lastIndex == -1 || lastIndex >= list.size()) {
                        throw new IllegalStateException("Tried to accept empty raycast!");
                    }
                    consumer.accept(list.get(lastIndex));
                }

                @Override
                public Optional<? extends Pair<? extends T, Double>> raycast() {
                    int best = -1;
                    double bestDist = Double.POSITIVE_INFINITY;
                    for (int i = lastIndex + 1; i < list.size(); i++) {
                        final T t = list.get(i);
                        final Iterator<Box> boxes = boxExtractor.apply(t);
                        while (boxes.hasNext()) {
                            final Box box = boxes.next();
                            final Optional<Vec3d> raycast = box.raycast(start, end);
                            if (raycast.isPresent()) {
                                final double dist = raycast.get().squaredDistanceTo(start);
                                if (dist < bestDist) {
                                    best = i;
                                    bestDist = dist;
                                }
                            }
                        }
                    }
                    if (best != -1) {
                        lastIndex = best;
                        return Optional.of(Pair.of(list.get(best), bestDist));
                    } else {
                        lastIndex = -1;
                        return Optional.empty();
                    }
                }
            };
        }

        static <T extends BattleParticipantActionTarget> TargetRaycaster<T> empty(final BooleanSupplier valid) {
            return new TargetRaycaster<>() {
                @Override
                public boolean valid() {
                    return valid.getAsBoolean();
                }

                @Override
                public void accept() {
                    throw new IllegalStateException("Tried to accept empty raycast!");
                }

                @Override
                public Optional<? extends Pair<? extends T, Double>> raycast() {
                    return Optional.empty();
                }
            };
        }

        @SafeVarargs
        static <T extends BattleParticipantActionTarget> TargetRaycaster<T> union(final BooleanSupplier valid, final TargetRaycaster<? extends T>... raycasters) {
            return new TargetRaycaster<>() {
                private int index = 0;

                @Override
                public boolean valid() {
                    return valid.getAsBoolean();
                }

                @Override
                public void accept() {
                    raycasters[index].accept();
                }

                @Override
                public Optional<? extends Pair<? extends T, Double>> raycast() {
                    Optional<? extends Pair<? extends T, Double>> opt = Optional.empty();
                    while (index < raycasters.length && (opt = raycasters[index].raycast()).isEmpty()) {
                        index++;
                    }
                    if (index == raycasters.length) {
                        return Optional.empty();
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

        boolean valid();

        static <T extends BattleParticipantActionTarget> TargetIterator<T> empty(final BooleanSupplier valid) {
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

                @Override
                public boolean valid() {
                    return valid.getAsBoolean();
                }
            };
        }

        static <T extends BattleParticipantActionTarget> TargetIterator<T> of(final Iterator<T> iterator, final Consumer<? super T> consumer, final BooleanSupplier valid) {
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

                @Override
                public boolean valid() {
                    return valid.getAsBoolean();
                }
            };
        }

        @SafeVarargs
        static <T extends BattleParticipantActionTarget> TargetIterator<T> union(final BooleanSupplier valid, final TargetIterator<? extends T>... iterators) {
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

                @Override
                public boolean valid() {
                    return valid.getAsBoolean();
                }
            };
        }
    }

    interface TargetProvider {
        <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(BattleParticipantActionTargetType<T> type);

        <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(BattleParticipantActionTargetType<T> type, Vec3d start, Vec3d end);

        Iterator<? extends BattleParticipantActionTargetType<?>> types();

        static TargetProvider empty(final BooleanSupplier valid) {
            return new TargetProvider() {
                @Override
                public <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(final BattleParticipantActionTargetType<T> type) {
                    return TargetIterator.empty(valid);
                }

                @Override
                public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type, final Vec3d start, final Vec3d end) {
                    return TargetRaycaster.empty(valid);
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return Collections.emptyIterator();
                }
            };
        }

        static <T extends BattleParticipantActionTarget> TargetProvider single(final BattleParticipantStateView stateView, final Consumer<BattleParticipantActionTarget> targetConsumer, final BooleanSupplier valid, final BattleParticipantActionTargetType<T> type, final Function3<BattleParticipantStateView, Consumer<BattleParticipantActionTarget>, BooleanSupplier, TargetIterator<T>> iteratorFactory, final TargetRaycasterFactory<T> raycastFactory) {
            return new TargetProvider() {
                @Override
                public <T0 extends BattleParticipantActionTarget> TargetIterator<? extends T0> targets(final BattleParticipantActionTargetType<T0> t) {
                    if (t == type) {
                        return (TargetIterator<? extends T0>) iteratorFactory.apply(stateView, targetConsumer, valid);
                    }
                    return TargetIterator.empty(valid);
                }

                @Override
                public <T0 extends BattleParticipantActionTarget> TargetRaycaster<? extends T0> raycastTargets(final BattleParticipantActionTargetType<T0> t, final Vec3d start, final Vec3d end) {
                    if (t == type) {
                        return (TargetRaycaster<? extends T0>) raycastFactory.build(stateView, targetConsumer, valid, start, end);
                    }
                    return TargetRaycaster.empty(valid);
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return Iterators.singletonIterator(type);
                }
            };
        }

        interface TargetRaycasterFactory<T extends BattleParticipantActionTarget> {
            TargetRaycaster<T> build(BattleParticipantStateView stateView, Consumer<BattleParticipantActionTarget> consumer, BooleanSupplier valid, Vec3d start, Vec3d end);
        }

        static TargetProvider typeCheck(final BooleanSupplier valid, final TargetProvider delegate, final BattleParticipantActionTargetType<?> type) {
            return typeCheck(valid, delegate, i -> i == type, () -> Iterators.singletonIterator(type));
        }

        static TargetProvider typeCheck(final BooleanSupplier valid, final TargetProvider delegate, final Predicate<BattleParticipantActionTargetType<?>> typeChecker, final Supplier<Iterator<? extends BattleParticipantActionTargetType<?>>> typeIterator) {
            return new TargetProvider() {
                @Override
                public <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(final BattleParticipantActionTargetType<T> type) {
                    if (typeChecker.test(type)) {
                        return delegate.targets(type);
                    }
                    return TargetIterator.empty(valid);
                }

                @Override
                public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type, final Vec3d start, final Vec3d end) {
                    if (typeChecker.test(type)) {
                        return delegate.raycastTargets(type, start, end);
                    }
                    return TargetRaycaster.empty(valid);
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return typeIterator.get();
                }
            };
        }

        static TargetProvider union(final BooleanSupplier valid, final TargetProvider... providers) {
            if (providers.length == 0) {
                return empty(valid);
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
                    return TargetIterator.union(valid, iterators);
                }

                @Override
                public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type, final Vec3d start, final Vec3d end) {
                    final TargetRaycaster<? extends T>[] raycasters = new TargetRaycaster[providers.length];
                    for (int i = 0; i < providers.length; i++) {
                        raycasters[i] = providers[i].raycastTargets(type, start, end);
                    }
                    return TargetRaycaster.union(valid, raycasters);
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return collect.iterator();
                }
            };
        }
    }

    interface TargetProviderFactory<T> {
        TargetProvider build(BattleParticipantStateView stateView, T state, Consumer<BattleParticipantActionTarget> targetConsumer, BooleanSupplier valid);

        @SafeVarargs
        static <S> TargetProviderFactory<S> union(final TargetProviderFactory<S>... factories) {
            return (stateView, state, targetConsumer, valid) -> {
                final TargetProvider[] providers = new TargetProvider[factories.length];
                for (int i = 0; i < factories.length; i++) {
                    providers[i] = factories[i].build(stateView, state, targetConsumer, valid);
                }
                return TargetProvider.union(valid, providers);
            };
        }

        static <T> TargetProviderFactory<T> orEmpty(final TargetProviderFactory<T> delegate, final Predicate<T> predicate) {
            return (stateView, state, targetConsumer, valid) -> predicate.test(state) ? delegate.build(stateView, state, targetConsumer, valid) : TargetProvider.empty(valid);
        }

        static <T> TargetProviderFactory<T> orElse(final TargetProviderFactory<T> delegate, final Predicate<T> predicate, final TargetProviderFactory<T> fallback) {
            return (stateView, state, targetConsumer, valid) -> predicate.test(state) ? delegate.build(stateView, state, targetConsumer, valid) : fallback.build(stateView, state, targetConsumer, valid);
        }
    }
}
