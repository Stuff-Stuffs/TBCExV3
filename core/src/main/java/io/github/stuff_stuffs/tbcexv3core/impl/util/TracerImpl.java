package io.github.stuff_stuffs.tbcexv3core.impl.util;

import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class TracerImpl<T> implements Tracer<T> {
    private final ObjectArrayList<Node<T>> stack = new ObjectArrayList<>();
    private final ObjectSet<IntervalStart<T>> open;
    private final List<IntervalStart<T>> starts;
    private final List<IntervalEnd<T>> ends;
    private final List<Instant<T>> instants;
    private final IntervalStartImpl<T> start;
    private final T endValue;

    public TracerImpl(final T startValue, final T endValue) {
        open = new ObjectOpenHashSet<>();
        start = new IntervalStartImpl<>(this, startValue, Map.of());
        open.add(start);
        this.endValue = endValue;
        starts = new ArrayList<>();
        ends = new ArrayList<>();
        instants = new ArrayList<>();
    }

    @Override
    public NodeBuilder<T, IntervalStart<T>> pushStart(final boolean defaultRelations) {
        return new NodeBuilderImpl<>(this, defaultRelations, (value, relations) -> {
            final IntervalStart<T> start = new IntervalStartImpl<>(TracerImpl.this, value, relations);
            starts.add(start);
            open.add(start);
            applyRelations(relations, start);
            stack.push(start);
            return start;
        });
    }

    private void applyRelations(final Map<Relation, Collection<Node<T>>> relations, final Node<T> owner) {
        final Function<Node<T>, Map<Relation, Collection<Node<T>>>> extractor = node -> {
            if (node instanceof IntervalStartImpl<T> start) {
                return start.reversedRelations;
            }
            if (node instanceof IntervalEndImpl<T> end) {
                return end.reversedRelations;
            }
            if (node instanceof InstantImpl<T> instant) {
                return instant.reversedRelations;
            }
            throw new RuntimeException();
        };
        for (final Map.Entry<Relation, Collection<Node<T>>> entry : relations.entrySet()) {
            final Relation key = entry.getKey();
            for (final Node<T> node : entry.getValue()) {
                extractor.apply(node).computeIfAbsent(key, i -> new ObjectOpenHashSet<>()).add(owner);
            }
        }
    }

    @Override
    public NodeBuilder<T, IntervalEnd<T>> pushEnd(final IntervalStart<T> start, final boolean defaultRelation) {
        if (start.parent() != this) {
            throw new RuntimeException();
        }
        return new NodeBuilderImpl<>(this, defaultRelation, (value, relations) -> {
            final IntervalEndImpl<T> end = new IntervalEndImpl<>(TracerImpl.this, start, value, relations);
            ends.add(end);
            if (!open.remove(start)) {
                throw new RuntimeException();
            }
            ((IntervalStartImpl<T>) start).end = end;
            applyRelations(relations, end);
            stack.push(end);
            return end;
        });
    }

    @Override
    public NodeBuilder<T, Instant<T>> pushInstant(final boolean defaultRelations) {
        return new NodeBuilderImpl<>(this, defaultRelations, (value, relations) -> {
            final Instant<T> instant = new InstantImpl<>(TracerImpl.this, value, relations);
            instants.add(instant);
            applyRelations(relations, instant);
            stack.push(instant);
            return instant;
        });
    }

    @Override
    public void pop() {
        if (stack.isEmpty()) {
            pushEnd(start).value(endValue).buildAndApply();
            stack.pop();
        } else {
            stack.pop();
        }
    }

    @Override
    public boolean checkForOpen() {
        return open.size() > 0;
    }

    @Override
    public IntervalStart<T> root() {
        return start;
    }

    @Override
    public Stream<Node<T>> all() {
        return Stream.concat(Stream.concat(starts(), ends()), instants());
    }

    @Override
    public Stream<IntervalStart<T>> starts() {
        return starts.stream();
    }

    @Override
    public Stream<IntervalEnd<T>> ends() {
        return ends.stream();
    }

    @Override
    public Stream<Instant<T>> instants() {
        return instants.stream();
    }

    private static final class NodeBuilderImpl<T, K extends Node<T>> implements NodeBuilder<T, K> {
        private T value;
        private final Map<Relation, Collection<Node<T>>> relations;
        private final TracerImpl<T> parent;
        private final boolean defaultRelation;
        private final BiFunction<T, Map<Relation, Collection<Node<T>>>, K> factory;
        private boolean built = false;

        private NodeBuilderImpl(final TracerImpl<T> parent, final boolean defaultRelation, final BiFunction<T, Map<Relation, Collection<Node<T>>>, K> factory) {
            this.defaultRelation = defaultRelation;
            this.factory = factory;
            relations = new Object2ReferenceOpenHashMap<>();
            this.parent = parent;
        }

        @Override
        public NodeBuilder<T, K> value(final T value) {
            if (built) {
                throw new RuntimeException();
            }
            this.value = value;
            return this;
        }

        @Override
        public NodeBuilder<T, K> relation(final Relation relation, final Node<T> node) {
            if (built) {
                throw new RuntimeException();
            }
            if (node.parent() != parent) {
                throw new RuntimeException();
            }
            relations.computeIfAbsent(relation, i -> new ObjectOpenHashSet<>()).add(node);
            return this;
        }

        @Override
        public K buildAndApply() {
            if (built || value == null) {
                throw new RuntimeException();
            }
            built = true;
            if (defaultRelation && parent.start != null) {
                relations.computeIfAbsent(TracerView.CAUSED_BY, i -> new ObjectOpenHashSet<>()).add(parent.stack.isEmpty() ? parent.start : parent.stack.top());
            }
            return factory.apply(value, relations);
        }
    }

    private static final class InstantImpl<T> implements Instant<T> {
        private final TracerImpl<T> parent;
        private final T value;
        private final Map<Relation, Collection<Node<T>>> relations;
        private final Map<Relation, Collection<Node<T>>> reversedRelations;

        private InstantImpl(final TracerImpl<T> parent, final T value, final Map<Relation, Collection<Node<T>>> relations) {
            this.parent = parent;
            this.value = value;
            this.relations = relations;
            reversedRelations = new Object2ReferenceOpenHashMap<>();
        }

        @Override
        public TracerView<T> parent() {
            return parent;
        }

        @Override
        public Map<Relation, Collection<Node<T>>> relations() {
            return Collections.unmodifiableMap(relations);
        }

        @Override
        public Map<Relation, Collection<Node<T>>> reversedRelations() {
            return Collections.unmodifiableMap(reversedRelations);
        }

        @Override
        public T value() {
            return value;
        }
    }

    private static final class IntervalEndImpl<T> implements IntervalEnd<T> {
        private final TracerImpl<T> parent;
        private final IntervalStart<T> start;
        private final T value;
        private final Map<Relation, Collection<Node<T>>> relations;
        private final Map<Relation, Collection<Node<T>>> reversedRelations;

        private IntervalEndImpl(final TracerImpl<T> parent, final IntervalStart<T> start, final T value, final Map<Relation, Collection<Node<T>>> relations) {
            this.parent = parent;
            this.start = start;
            this.value = value;
            this.relations = relations;
            reversedRelations = new Object2ReferenceOpenHashMap<>();
        }

        @Override
        public TracerView<T> parent() {
            return parent;
        }

        @Override
        public Map<Relation, Collection<Node<T>>> relations() {
            return Collections.unmodifiableMap(relations);
        }

        @Override
        public Map<Relation, Collection<Node<T>>> reversedRelations() {
            return Collections.unmodifiableMap(reversedRelations);
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public IntervalStart<T> start() {
            return start;
        }
    }

    private static final class IntervalStartImpl<T> implements IntervalStart<T> {
        private final TracerImpl<T> parent;
        private final T value;
        private final Map<Relation, Collection<Node<T>>> relations;
        private final Map<Relation, Collection<Node<T>>> reversedRelations;
        private @Nullable IntervalEnd<T> end;

        private IntervalStartImpl(final TracerImpl<T> parent, final T value, final Map<Relation, Collection<Node<T>>> relations) {
            this.parent = parent;
            this.value = value;
            this.relations = relations;
            reversedRelations = new Object2ReferenceOpenHashMap<>();
            end = null;
        }

        @Override
        public TracerView<T> parent() {
            return parent;
        }

        @Override
        public Map<Relation, Collection<Node<T>>> relations() {
            return Collections.unmodifiableMap(relations);
        }

        @Override
        public Map<Relation, Collection<Node<T>>> reversedRelations() {
            return Collections.unmodifiableMap(reversedRelations);
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public Optional<IntervalEnd<T>> end() {
            return Optional.ofNullable(end);
        }
    }
}
