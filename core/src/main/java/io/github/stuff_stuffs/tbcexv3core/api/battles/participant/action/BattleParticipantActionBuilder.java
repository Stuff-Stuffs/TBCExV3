package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.action.BattleParticipantActionBuilderImpl;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ApiStatus.NonExtendable
public interface BattleParticipantActionBuilder {
    <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(BattleParticipantActionTargetType<T> type);

    <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(BattleParticipantActionTargetType<T> type, Vec3d start, Vec3d end, double max);

    Iterator<? extends BattleParticipantActionTargetType<?>> types();

    boolean canBuild();

    BattleAction build();

    static <S> BattleParticipantActionBuilder create(final BattleParticipantStateView view, final Predicate<S> predicate, final Function<S, BattleAction> builder, final S state, final TargetProviderFactory<S> factory, final BiConsumer<S, BattleParticipantActionTarget> updater) {
        return new BattleParticipantActionBuilderImpl<>(view, predicate, builder, state, factory, updater);
    }

    interface TargetRaycaster<T extends BattleParticipantActionTarget> {
        void skip();

        boolean valid();

        void accept();

        OptionalDouble raycast();

        static <T extends BattleParticipantActionTarget> TargetRaycaster<T> empty(final BooleanSupplier valid) {
            return new TargetRaycaster<>() {
                @Override
                public void skip() {
                    throw new IllegalStateException();
                }

                @Override
                public boolean valid() {
                    return valid.getAsBoolean();
                }

                @Override
                public void accept() {
                    throw new IllegalStateException();
                }

                @Override
                public OptionalDouble raycast() {
                    return OptionalDouble.empty();
                }
            };
        }

        @SafeVarargs
        static <T extends BattleParticipantActionTarget> TargetRaycaster<T> union(final BooleanSupplier valid, final TargetRaycaster<? extends T>... raycasters) {
            return new TargetRaycaster<>() {
                private int index = 0;

                @Override
                public void skip() {
                    raycasters[index].skip();
                }

                @Override
                public boolean valid() {
                    return valid.getAsBoolean();
                }

                @Override
                public void accept() {
                    raycasters[index].accept();
                }

                @Override
                public OptionalDouble raycast() {
                    OptionalDouble opt = OptionalDouble.empty();
                    while (index < raycasters.length && (opt = raycasters[index].raycast()).isEmpty()) {
                        index++;
                    }
                    if (index == raycasters.length) {
                        return OptionalDouble.empty();
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

        <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(BattleParticipantActionTargetType<T> type, Vec3d start, Vec3d end, double max);

        Iterator<? extends BattleParticipantActionTargetType<?>> types();

        static TargetProvider empty(final BooleanSupplier valid) {
            return new TargetProvider() {
                @Override
                public <T extends BattleParticipantActionTarget> TargetIterator<? extends T> targets(final BattleParticipantActionTargetType<T> type) {
                    return TargetIterator.empty(valid);
                }

                @Override
                public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type, final Vec3d start, final Vec3d end, final double max) {
                    return TargetRaycaster.empty(valid);
                }

                @Override
                public Iterator<? extends BattleParticipantActionTargetType<?>> types() {
                    return Collections.emptyIterator();
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
                public <T extends BattleParticipantActionTarget> TargetRaycaster<? extends T> raycastTargets(final BattleParticipantActionTargetType<T> type, final Vec3d start, final Vec3d end, final double max) {
                    final TargetRaycaster<? extends T>[] raycasters = new TargetRaycaster[providers.length];
                    for (int i = 0; i < providers.length; i++) {
                        raycasters[i] = providers[i].raycastTargets(type, start, end, max);
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
    }
}
