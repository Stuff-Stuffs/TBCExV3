package io.github.stuff_stuffs.tbcexv3core.api.util;

import io.github.stuff_stuffs.tbcexv3core.impl.util.TracerImpl;

import java.util.Optional;

public interface Tracer<T> extends TracerView<T> {
    default NodeBuilder<T, IntervalStart<T>> pushStart() {
        return pushStart(true);
    }

    NodeBuilder<T, IntervalStart<T>> pushStart(boolean defaultRelation);

    default NodeBuilder<T, IntervalEnd<T>> pushEnd(final IntervalStart<T> start) {
        return pushEnd(start, true);
    }

    NodeBuilder<T, IntervalEnd<T>> pushEnd(IntervalStart<T> start, boolean defaultRelation);

    default NodeBuilder<T, Instant<T>> pushInstant() {
        return pushInstant(true);
    }

    NodeBuilder<T, Instant<T>> pushInstant(boolean defaultRelation);

    void pop();

    boolean checkForOpen();

    interface NodeBuilder<T, K extends Node<T>> {
        NodeBuilder<T, K> value(T value);

        NodeBuilder<T, K> relation(Relation relation, Node<T> node);

        K buildAndApply();
    }

    static <T> Tracer<T> create(final T startValue, final T endValue) {
        return new TracerImpl<>(startValue, endValue);
    }
}
