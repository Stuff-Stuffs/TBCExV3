package io.github.stuff_stuffs.tbcexv3core.api.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.NonExtendable
public interface TracerView<T> {
    Stream<Node<T>> all();

    Stream<StartNode<T>> starts();

    Stream<EndNode<T>> ends();

    TracerView<T> before(Node<T> node);

    TracerView<T> after(Node<T> node);

    static <T> TracerView<T> empty() {
        return new TracerView<>() {
            @Override
            public Stream<Node<T>> all() {
                return Stream.empty();
            }

            @Override
            public Stream<StartNode<T>> starts() {
                return Stream.empty();
            }

            @Override
            public Stream<EndNode<T>> ends() {
                return Stream.empty();
            }

            @Override
            public TracerView<T> before(final Node<T> node) {
                return this;
            }

            @Override
            public TracerView<T> after(final Node<T> node) {
                return this;
            }
        };
    }

    sealed interface Node<T> {
        TracerView<T> parent();

        Optional<Node<T>> causedBy();

        List<Node<T>> caused();

        T value();
    }

    non-sealed interface StartNode<T> extends Node<T> {
        Optional<EndNode<T>> end();
    }

    non-sealed interface EndNode<T> extends Node<T> {
        Node<T> start();
    }
}
