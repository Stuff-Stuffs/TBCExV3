package io.github.stuff_stuffs.tbcexv3core.impl.util;

import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public class TracerImpl<T> implements Tracer<T> {
    private final StageImpl<T> root;
    private final ObjectArrayList<StageImpl<T>> stack;

    public TracerImpl(final T rootValue) {
        root = new StageImpl<>(null, rootValue);
        stack = new ObjectArrayList<>();
    }

    @Override
    public void push(final T value) {
        final StageImpl<T> parent;
        if (stack.isEmpty()) {
            parent = root;
        } else {
            parent = stack.top();
        }
        final StageImpl<T> current = new StageImpl<>(parent, value);
        parent.children.add(current);
        stack.push(current);
    }

    @Override
    public void pop() {
        if (!stack.isEmpty()) {
            stack.pop();
        } else {
            throw new TBCExException("Cannot pop root value of tracer!");
        }
    }

    @Override
    public Stage<T> rootStage() {
        return root;
    }

    @Override
    public StageImpl<T> activeStage() {
        return stack.isEmpty() ? root : stack.top();
    }

    @Override
    public Stream<Stage<T>> leaves(final boolean includeUnfinishedLeaves) {
        final Stream.Builder<Stage<T>> builder = Stream.builder();
        addLeaves(builder, root, includeUnfinishedLeaves ? null : activeStage());
        return builder.build();
    }

    private void addLeaves(final Stream.Builder<Stage<T>> builder, final StageImpl<T> node, @Nullable final StageImpl<T> exclude) {
        if (node.children.isEmpty()) {
            if (node != exclude) {
                builder.accept(node);
            }
        } else {
            for (final StageImpl<T> child : node.children) {
                addLeaves(builder, child, exclude);
            }
        }
    }

    private static final class StageImpl<T> implements Tracer.Stage<T> {
        private final @Nullable StageImpl<T> parent;
        private final Collection<StageImpl<T>> children;
        private final T value;

        private StageImpl(@Nullable final StageImpl<T> parent, final T value) {
            this.parent = parent;
            this.value = value;
            children = new ArrayList<>();
        }

        @Override
        public Optional<Stage<T>> parent() {
            return Optional.ofNullable(parent);
        }

        @Override
        public Collection<Stage<T>> children() {
            return Collections.unmodifiableCollection(children);
        }

        @Override
        public T value() {
            return value;
        }
    }
}
