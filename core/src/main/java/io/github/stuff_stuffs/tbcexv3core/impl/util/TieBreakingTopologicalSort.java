package io.github.stuff_stuffs.tbcexv3core.impl.util;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntHeapPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class TieBreakingTopologicalSort {
    public static <T> List<T> sort(final List<T> items, final ChildPredicate<T> childPredicate, final Comparator<T> tieBreaker) {
        final Set<T> set = new ObjectOpenHashSet<>();
        final Int2IntOpenHashMap indexToDependencyCount = new Int2IntOpenHashMap();
        final int size = items.size();
        for (int i = 0; i < size; i++) {
            final T item = items.get(i);
            if (!set.add(item)) {
                throw new IllegalStateException("Duplicate items detected!");
            }
            for (int j = 0; j < size; j++) {
                if (childPredicate.isChild(i, j, items)) {
                    indexToDependencyCount.addTo(j, 1);
                }
            }
        }
        final IntPriorityQueue queue = new IntHeapPriorityQueue((i0, i1) -> tieBreaker.compare(items.get(i0), items.get(i1)));
        final List<T> output = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            if (queue.isEmpty()) {
                throw new IllegalStateException("Cycle detected!");
            }
            final int idx = queue.dequeueInt();
            final T item = items.get(idx);
            output.add(item);
            for (int j = 0; j < size; j++) {
                if (childPredicate.isChild(i, j, items)) {
                    if (indexToDependencyCount.addTo(j, -1) == 1) {
                        queue.enqueue(j);
                    }
                }
            }
        }
        return output;
    }

    public interface ChildPredicate<T> {
        boolean isChild(int parent, int child, List<T> items);
    }

    private TieBreakingTopologicalSort() {
    }
}
