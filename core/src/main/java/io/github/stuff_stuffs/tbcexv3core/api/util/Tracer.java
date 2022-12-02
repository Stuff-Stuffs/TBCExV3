package io.github.stuff_stuffs.tbcexv3core.api.util;

import io.github.stuff_stuffs.tbcexv3core.impl.util.TracerImpl;

public interface Tracer<T> extends TracerView<T> {
    void push(T value);

    void pop();

    static <T> Tracer<T> create(final T rootValue) {
        return new TracerImpl<>(rootValue);
    }
}
