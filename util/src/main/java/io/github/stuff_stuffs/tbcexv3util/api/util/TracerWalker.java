package io.github.stuff_stuffs.tbcexv3util.api.util;

import io.github.stuff_stuffs.tbcexv3util.impl.util.TracerWalkerBuilderImpl;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface TracerWalker<T, S> {
    boolean accept(TracerView.Node<T> node);

    S getState();

    interface Builder<T, S> {
        Builder<T, S> add(Predicate<TracerView.Node<T>> predicate, BiFunction<TracerView.Node<T>, S, S> stateUpdater);

        TracerWalker<T, S> build(S initial);
    }

    static <T, S> Builder<T, S> builder() {
        return new TracerWalkerBuilderImpl<>();
    }

    static <T, S0, S1, SC> TracerWalker<T, SC> union(final TracerWalker<T, S0> first, final TracerWalker<T, S1> second, final BiFunction<S0, S1, SC> combiner) {
        return new TracerWalker<>() {
            private SC combined = combiner.apply(first.getState(), second.getState());

            @Override
            public boolean accept(final TracerView.Node<T> node) {
                final boolean f = first.accept(node);
                final boolean s = second.accept(node);
                if (f | s) {
                    combined = combiner.apply(first.getState(), second.getState());
                    return true;
                }
                return false;
            }

            @Override
            public SC getState() {
                return combined;
            }
        };
    }
}
