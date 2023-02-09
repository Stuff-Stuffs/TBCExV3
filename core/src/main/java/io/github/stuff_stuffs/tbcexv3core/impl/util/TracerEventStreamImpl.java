package io.github.stuff_stuffs.tbcexv3core.impl.util;

import io.github.stuff_stuffs.tbcexv3core.api.util.TracerEventStream;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class TracerEventStreamImpl<T> implements TracerEventStream<T> {
    private final Set<TracerView.NodeId> seen = new ObjectOpenHashSet<>();
    private final List<TracerView.Node<T>> toUpdate = new ArrayList<>();

    @Override
    public void update(final TracerView<T> tracer) {
        tracer.all().filter(node -> seen.add(node.id())).forEach(toUpdate::add);
    }

    @Override
    public Stream<TracerView.Node<T>> newEvents() {
        final Stream<TracerView.Node<T>> stream = new ArrayList<>(toUpdate).stream();
        toUpdate.clear();
        return stream;
    }
}
