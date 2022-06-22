package io.github.stuff_stuffs.tbcexv3core.api.util;

import io.github.stuff_stuffs.tbcexv3core.impl.util.TracerImpl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface Tracer<T> {
    void push(T value);

    void pop();

    Stage<T> getCurrentStage();

    Stream<Stage<T>> getLeaves(boolean includeUnfinishedLeaves);

    interface Stage<T> {
        Optional<Stage<T>> getParent();

        Collection<Stage<T>> getChildren();

        T getValue();
    }

    static <T> Tracer<T> create(final T rootValue) {
        return new TracerImpl<>(rootValue);
    }
}
