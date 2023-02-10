package io.github.stuff_stuffs.tbcexv3util.api.util;

import io.github.stuff_stuffs.tbcexv3util.impl.util.TracerEventStreamImpl;

import java.util.stream.Stream;

public interface TracerEventStream<T> {
    void update(TracerView<T> tracer);

    Stream<TracerView.Node<T>> newEvents();

    static <T> TracerEventStream<T> create() {
        return new TracerEventStreamImpl<>();
    }
}
