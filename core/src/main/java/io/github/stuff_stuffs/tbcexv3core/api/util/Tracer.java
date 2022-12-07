package io.github.stuff_stuffs.tbcexv3core.api.util;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.impl.util.TracerImpl;

public interface Tracer<T> extends TracerView<T> {
    StartNode<T> pushStart(T value);

    EndNode<T> pushEnd(T value, StartNode<T> start);

    default Pair<StartNode<T>, EndNode<T>> pushInstant(final T start, final T end) {
        final StartNode<T> startNode = pushStart(start);
        pop();
        final EndNode<T> endNode = pushEnd(end, startNode);
        return Pair.of(startNode, endNode);
    }

    void pop();

    boolean checkForOpen();

    static <T> Tracer<T> create(final T startValue, final T endValue) {
        return new TracerImpl<>(startValue, endValue);
    }
}
