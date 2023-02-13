package io.github.stuff_stuffs.tbcexv3util.impl.util;

import io.github.stuff_stuffs.tbcexv3util.api.util.Pathfinder;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeaps;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class PathfinderImpl {
    private PathfinderImpl() {
    }

    public static <T> Pathfinder.PathTree<T> find(final BlockPos start, final Pathfinder.NeighbourGetter getter, final Pathfinder.PostProcessor<T> processor) {
        final Object2ReferenceMap<BlockPos, NodeImpl> nodes = new Object2ReferenceOpenHashMap<>();
        final PathQueue queue = new PathQueue();
        getter.start(start, new Pathfinder.NodeAppender() {
            @Override
            public void append(final BlockPos pos, final double cost) {
                final BlockPos immutable = pos.toImmutable();
                final NodeImpl n = new NodeImpl(immutable, cost);
                n.enqueued = true;
                nodes.put(immutable, n);
                queue.enqueue(n);
            }

            @Override
            public double getCost(final BlockPos pos) {
                return nodes.get(pos).cost;
            }
        });
        while (!queue.isEmpty()) {
            final NodeImpl node = queue.dequeue();
            node.enqueued = false;
            getter.neighbours(node.pos, node, new Pathfinder.NodeAppender() {
                @Override
                public void append(final BlockPos pos, final double cost) {
                    NodeImpl n = nodes.get(pos);
                    if (n != null) {
                        if (n.cost > cost + 0.000001) {
                            n.cost = cost;
                            n.previous = node;
                            if (n.enqueued) {
                                queue.decrease(n);
                            } else {
                                node.enqueued = true;
                                queue.enqueue(n);
                            }
                        }
                    } else {
                        final BlockPos immutable = pos.toImmutable();
                        n = new NodeImpl(immutable, cost);
                        n.enqueued = true;
                        n.previous = node;
                        nodes.put(immutable, n);
                        queue.enqueue(n);
                    }
                }

                @Override
                public double getCost(final BlockPos pos) {
                    final NodeImpl n = nodes.get(pos);
                    return n == null ? Double.POSITIVE_INFINITY : n.cost;
                }
            });
        }
        final Map<BlockPos, T> paths = new Object2ReferenceOpenHashMap<>();
        for (final NodeImpl value : nodes.values()) {
            if (processor.isValidEndPoint(value)) {
                final LinkedList<BlockPos> l = new LinkedList<>();
                NodeImpl n = value;
                do {
                    l.addFirst(n.pos);
                    n = n.previous;
                } while (n != null);
                paths.put(value.pos, processor.process(new ArrayList<>(l)));
            }
        }
        return new Pathfinder.PathTree<T>() {
            @Override
            public Set<BlockPos> endPositions() {
                return Collections.unmodifiableSet(paths.keySet());
            }

            @Override
            public T getPath(final BlockPos end) {
                return paths.get(end);
            }
        };
    }

    private static final class NodeImpl implements Pathfinder.Node, Comparable<NodeImpl> {
        private final BlockPos pos;
        private NodeImpl previous;
        private double cost;
        private boolean enqueued = false;

        private NodeImpl(final BlockPos pos, final double cost) {
            this.pos = pos;
            this.cost = cost;
        }

        @Override
        public BlockPos pos() {
            return pos;
        }

        @Override
        public double cost() {
            return cost;
        }

        @Override
        public Pathfinder.@Nullable Node previous() {
            return previous;
        }

        @Override
        public int compareTo(final PathfinderImpl.NodeImpl o) {
            return Double.compare(cost, o.cost);
        }
    }

    private static final class PathQueue extends ObjectHeapPriorityQueue<NodeImpl> {
        public void decrease(final NodeImpl node) {
            for (int i = 0; i < size; i++) {
                if (heap[size] == node) {
                    ObjectHeaps.downHeap(heap, size, i, c);
                    return;
                }
            }
        }
    }
}
