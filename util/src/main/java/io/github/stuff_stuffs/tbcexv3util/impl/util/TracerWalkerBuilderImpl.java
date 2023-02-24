package io.github.stuff_stuffs.tbcexv3util.impl.util;

import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerWalker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class TracerWalkerBuilderImpl<T, S> implements TracerWalker.Builder<T, S> {
    private final List<Entry<T, S>> entries = new ArrayList<>();

    @Override
    public TracerWalker.Builder<T, S> add(final Predicate<TracerView.Node<T>> predicate, final BiFunction<TracerView.Node<T>, S, S> stateUpdater) {
        entries.add(new Entry<>(predicate, stateUpdater));
        return null;
    }

    @Override
    public TracerWalker<T, S> build(final S initial) {
        final List<Entry<T, S>> copy = new ArrayList<>(entries);
        return new TracerWalker<T, S>() {
            private S state = initial;

            @Override
            public boolean accept(final TracerView.Node<T> node) {
                boolean changed = false;
                for (final Entry<T, S> entry : copy) {
                    if (entry.predicate.test(node)) {
                        state = entry.stateUpdater.apply(node, state);
                        changed = true;
                    }
                }
                return changed;
            }

            @Override
            public S getState() {
                return state;
            }
        };
    }

    private record Entry<T, S>(
            Predicate<TracerView.Node<T>> predicate,
            BiFunction<TracerView.Node<T>, S, S> stateUpdater
    ) {
    }
}
