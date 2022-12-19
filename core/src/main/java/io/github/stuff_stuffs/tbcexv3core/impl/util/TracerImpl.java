package io.github.stuff_stuffs.tbcexv3core.impl.util;

import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class TracerImpl<T> implements Tracer<T> {
    private final Long2ObjectMap<StartNodeImpl<T>> open = new Long2ObjectOpenHashMap<>();
    private final StartNodeImpl<T> start;
    private final T endValue;
    private boolean finished = false;
    private final ObjectArrayList<Node<T>> stack = new ObjectArrayList<>();
    private long nextId = 0;

    public TracerImpl(final T startValue, final T endValue) {
        start = new StartNodeImpl<>(nextId++, new int[0], this, null, startValue);
        open.put(start.id, start);
        this.endValue = endValue;
    }

    @Override
    public StartNode<T> pushStart(final T value) {
        final Node<T> cursor = cursor();
        final int[] prevPath = path(cursor);
        final int index = cursor.caused().size();
        final int[] newPath = Arrays.copyOf(prevPath, prevPath.length + 1);
        newPath[newPath.length - 1] = index;
        final StartNodeImpl<T> node = new StartNodeImpl<>(nextId++, newPath, this, cursor, value);
        switch (cursor) {
            case StartNode<T> startNode -> ((StartNodeImpl<T>) startNode).addChild(node);
            case EndNode<T> endNode -> ((EndNodeImpl<T>) endNode).addChild(node);
        }
        open.put(node.id, node);
        stack.push(node);
        return node;
    }

    @Override
    public EndNode<T> pushEnd(final T value, final StartNode<T> start) {
        final StartNodeImpl<T> impl = (StartNodeImpl<T>) start;
        if (impl.parent != this || impl.end != null) {
            throw new RuntimeException();
        }
        final Node<T> cursor = cursor();
        final int[] prevPath = path(cursor);
        final int index = cursor.caused().size();
        final int[] newPath = Arrays.copyOf(prevPath, prevPath.length + 1);
        newPath[newPath.length - 1] = index;
        final EndNodeImpl<T> node = new EndNodeImpl<>(this, newPath, cursor, value, start);
        switch (cursor) {
            case StartNode<T> startNode -> ((StartNodeImpl<T>) startNode).addChild(node);
            case EndNode<T> endNode -> ((EndNodeImpl<T>) endNode).addChild(node);
        }
        open.remove(impl.id);
        impl.end = node;
        stack.push(node);
        return node;
    }

    @Override
    public void pop() {
        if (!stack.isEmpty()) {
            stack.pop();
        } else {
            if (finished) {
                throw new RuntimeException();
            }
            pushEnd(endValue, start);
            finished = true;
        }
    }

    @Override
    public boolean checkForOpen() {
        return !open.isEmpty();
    }

    private record StackEntry<T>(Node<T> node, int index) {
    }

    @Override
    public Stream<Node<T>> all() {
        final Stream.Builder<Node<T>> builder = Stream.builder();
        final Stack<StackEntry<T>> stack = new ObjectArrayList<>();
        stack.push(new StackEntry<>(start, 0));
        builder.add(start);
        while (!stack.isEmpty()) {
            final StackEntry<T> entry = stack.pop();
            final List<Node<T>> caused = entry.node().caused();
            if (entry.index() < caused.size()) {
                final Node<T> node = caused.get(entry.index());
                builder.add(node);
                stack.push(new StackEntry<>(entry.node(), entry.index() + 1));
                stack.push(new StackEntry<>(node, 0));
            }
        }
        return builder.build();
    }

    @Override
    public Stream<StartNode<T>> starts() {
        return all().map(node -> {
            if (node instanceof StartNode<T> startNode) {
                return startNode;
            } else {
                return null;
            }
        }).filter(Objects::nonNull);
    }

    @Override
    public Stream<EndNode<T>> ends() {
        return all().map(node -> {
            if (node instanceof EndNode<T> endNode) {
                return endNode;
            } else {
                return null;
            }
        }).filter(Objects::nonNull);
    }

    @Override
    public TracerView<T> before(final Node<T> node) {
        return new SubView<>(start, path(node), new int[0]);
    }

    @Override
    public TracerView<T> after(final Node<T> node) {
        return new SubView<>(start, new int[0], path(node));
    }

    private static int[] path(final Node<?> node) {
        return switch (node) {
            case StartNode<?> startNode -> ((StartNodeImpl<?>) startNode).path;
            case EndNode<?> endNode -> ((EndNodeImpl<?>) endNode).path;
        };
    }

    private Node<T> cursor() {
        return stack.isEmpty() ? start : stack.top();
    }

    private static final class StartNodeImpl<T> implements StartNode<T> {
        private final long id;
        private final int[] path;
        private final TracerImpl<T> parent;
        private final @Nullable Node<T> causedBy;
        private final T value;
        private @Nullable EndNode<T> end;
        private List<Node<T>> children;

        private StartNodeImpl(final long id, final int[] path, final TracerImpl<T> parent, @Nullable final Node<T> by, final T value) {
            this.id = id;
            this.path = path;
            this.parent = parent;
            causedBy = by;
            this.value = value;
            children = Collections.emptyList();
        }

        @Override
        public TracerView<T> parent() {
            return parent;
        }

        @Override
        public Optional<Node<T>> causedBy() {
            return Optional.ofNullable(causedBy);
        }

        @Override
        public List<Node<T>> caused() {
            return Collections.unmodifiableList(children);
        }

        private void addChild(final Node<T> node) {
            if (node.parent() != parent) {
                throw new RuntimeException();
            }
            final List<Node<T>> copy = new ArrayList<>(children.size() + 1);
            copy.addAll(children);
            copy.add(node);
            children = copy;
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public Optional<EndNode<T>> end() {
            return Optional.ofNullable(end);
        }
    }

    private static final class EndNodeImpl<T> implements EndNode<T> {
        private final TracerImpl<T> parent;
        private final int[] path;
        private final @Nullable Node<T> causedBy;
        private final T value;
        private final StartNode<T> start;
        private List<Node<T>> children;

        private EndNodeImpl(final TracerImpl<T> parent, final int[] path, @Nullable final Node<T> by, final T value, final StartNode<T> start) {
            this.parent = parent;
            this.path = path;
            causedBy = by;
            this.value = value;
            this.start = start;
            children = Collections.emptyList();
        }

        @Override
        public TracerView<T> parent() {
            return parent;
        }

        @Override
        public Optional<Node<T>> causedBy() {
            return Optional.ofNullable(causedBy);
        }

        @Override
        public List<Node<T>> caused() {
            return Collections.unmodifiableList(children);
        }

        private void addChild(final Node<T> node) {
            if (node.parent() != parent) {
                throw new RuntimeException();
            }
            final List<Node<T>> copy = new ArrayList<>(children.size() + 1);
            copy.addAll(children);
            copy.add(node);
            children = copy;
        }

        @Override
        public T value() {
            return value;
        }

        @Override
        public Node<T> start() {
            return start;
        }
    }

    private static final class SubView<T> implements TracerView<T> {
        private final Node<T> node;
        private final int[] before;
        private final int[] after;

        private SubView(final Node<T> node, final int[] before, final int[] after) {
            this.node = node;
            this.before = before;
            this.after = after;
        }

        @Override
        public Stream<Node<T>> all() {
            return stream();
        }

        @Override
        public Stream<StartNode<T>> starts() {
            return all().map(node -> {
                if (node instanceof StartNode<T> startNode) {
                    return startNode;
                } else {
                    return null;
                }
            }).filter(Objects::nonNull);
        }

        @Override
        public Stream<EndNode<T>> ends() {
            return all().map(node -> {
                if (node instanceof EndNode<T> endNode) {
                    return endNode;
                } else {
                    return null;
                }
            }).filter(Objects::nonNull);
        }

        @Override
        public TracerView<T> before(final Node<T> node) {
            final int[] path = path(node);
            final int compare = Arrays.compare(before, path);
            if (compare == 0) {
                return this;
            }
            return compare < 0 ? new SubView<>(this.node, path, after) : TracerView.empty();
        }

        @Override
        public TracerView<T> after(final Node<T> node) {
            final int[] path = path(node);
            final int compare = Arrays.compare(after, path);
            if (compare == 0) {
                return this;
            }
            return compare > 0 ? new SubView<>(this.node, before, path) : TracerView.empty();
        }

        private Stream<Node<T>> stream() {
            final Stream.Builder<Node<T>> builder = Stream.builder();
            final Stack<StackEntry<T>> stack = new ObjectArrayList<>();
            stack.push(new StackEntry<>(node, 0));
            builder.add(node);
            while (!stack.isEmpty()) {
                final StackEntry<T> entry = stack.pop();
                final List<Node<T>> caused = entry.node().caused();
                if (entry.index() < caused.size()) {
                    final Node<T> node = caused.get(entry.index());
                    final int[] path = path(node);
                    if (Arrays.compare(before, path) > 0 && Arrays.compare(after, path) < 0) {
                        builder.add(node);
                    }
                    stack.push(new StackEntry<>(entry.node(), entry.index() + 1));
                    stack.push(new StackEntry<>(node, 0));
                }
            }
            return builder.build();
        }
    }
}
