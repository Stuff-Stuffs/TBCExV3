package io.github.stuff_stuffs.tbcexv3core.api.util;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface TracerView<T> {
    Stage<T> rootStage();

    Stage<T> activeStage();

    Stream<Stage<T>> leaves(boolean includeUnfinishedLeaves);

    interface Stage<T> {
        Optional<Tracer.Stage<T>> parent();

        Collection<Tracer.Stage<T>> children();

        T value();
    }
}
