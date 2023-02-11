package io.github.stuff_stuffs.tbcexv3util.api.util;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.NonExtendable
public interface TracerView<T> {
    Relation CAUSED_BY = createRelation(new Identifier("tbcexv3_core", "caused_by"));

    IntervalStart<T> root();

    Stream<Node<T>> all();

    Stream<IntervalStart<T>> starts();

    Stream<IntervalEnd<T>> ends();

    Stream<Instant<T>> instants();

    static <T> TracerView<T> empty(final IntervalStart<T> root) {
        return new TracerView<>() {
            @Override
            public IntervalStart<T> root() {
                return root;
            }

            @Override
            public Stream<Node<T>> all() {
                return Stream.empty();
            }

            @Override
            public Stream<IntervalStart<T>> starts() {
                return Stream.empty();
            }

            @Override
            public Stream<IntervalEnd<T>> ends() {
                return Stream.empty();
            }

            @Override
            public Stream<Instant<T>> instants() {
                return Stream.empty();
            }
        };
    }

    static Relation createRelation(final Identifier id) {
        return new Relation() {
            @Override
            public Identifier id() {
                return id;
            }

            @Override
            public int hashCode() {
                return id.hashCode();
            }

            @Override
            public boolean equals(final Object obj) {
                if (obj instanceof Relation relation) {
                    return id.equals(relation.id());
                } else {
                    return false;
                }
            }
        };
    }

    interface Relation {
        Identifier id();
    }

    sealed interface Node<T> {
        TracerView<T> parent();

        Map<Relation, Collection<Node<T>>> relations();

        Map<Relation, Collection<Node<T>>> reversedRelations();

        T value();

        NodeId id();
    }

    @ApiStatus.NonExtendable
    non-sealed interface Instant<T> extends Node<T> {
    }

    @ApiStatus.NonExtendable
    non-sealed interface IntervalStart<T> extends Node<T> {
        Optional<IntervalEnd<T>> end();
    }

    @ApiStatus.NonExtendable
    non-sealed interface IntervalEnd<T> extends Node<T> {
        IntervalStart<T> start();
    }

    @ApiStatus.NonExtendable
    interface NodeId {
        @Override
        int hashCode();

        @Override
        boolean equals(Object other);
    }
}
