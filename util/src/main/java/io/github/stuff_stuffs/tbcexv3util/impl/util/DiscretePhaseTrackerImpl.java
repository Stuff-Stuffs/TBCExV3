package io.github.stuff_stuffs.tbcexv3util.impl.util;

import io.github.stuff_stuffs.tbcexv3util.api.util.DiscretePhaseTracker;
import io.github.stuff_stuffs.tbcexv3util.api.util.TopologicalSort;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.util.Identifier;

import java.util.*;

public class DiscretePhaseTrackerImpl implements DiscretePhaseTracker {
    private final Map<Identifier, Relation> relations;
    private final Object2IntMap<Identifier> order;
    private final List<Runnable> listeners;

    public DiscretePhaseTrackerImpl() {
        relations = new Object2ReferenceOpenHashMap<>();
        order = new Object2IntOpenHashMap<>();
        listeners = new ArrayList<>();
    }

    @Override
    public Set<Identifier> phases() {
        return Collections.unmodifiableSet(relations.keySet());
    }


    @Override
    public void addPhase(final Identifier identifier, final Set<Identifier> phasesBefore, final Set<Identifier> phasesAfter) {
        if (relations.put(identifier, new Relation(Set.copyOf(phasesBefore), Set.copyOf(phasesAfter))) != null) {
            throw new RuntimeException("Duplicate phases: " + identifier);
        }
        sort();
    }

    @Override
    public void addRelation(final Identifier identifier, final Set<Identifier> before, final Set<Identifier> after) {
        final Relation relation = relations.get(identifier);
        if (relation == null) {
            addPhase(identifier, before, after);
        } else {
            final Set<Identifier> b = new ObjectOpenHashSet<>(before.size() + relation.before.size());
            final Set<Identifier> a = new ObjectOpenHashSet<>(after.size() + relation.after.size());
            b.addAll(before);
            b.addAll(relation.before);
            a.addAll(after);
            a.addAll(relation.after);
            relations.put(identifier, new Relation(b, a));
            sort();
        }
    }

    private void sort() {
        final ObjectArrayList<Identifier> items = new ObjectArrayList<>(relations.keySet());
        final List<Identifier> sorted = TopologicalSort.tieBreakingSort(items, (parent, child, items1) -> {
            final Identifier parentId = items1.get(parent);
            final Identifier childId = items1.get(child);
            if (relations.get(parentId).after.contains(childId)) {
                return true;
            } else if (relations.get(childId).before.contains(parentId)) {
                return true;
            }
            return false;
        }, Identifier::compareTo);
        order.clear();
        final int size = sorted.size();
        for (int i = 0; i < size; i++) {
            order.put(sorted.get(i), i);
        }
        for (final Runnable listener : listeners) {
            listener.run();
        }
    }

    @Override
    public Comparator<Identifier> phaseComparator() {
        return Comparator.comparingInt(id -> {
            final int i = order.getOrDefault(id, -1);
            if (i == -1) {
                throw new RuntimeException();
            }
            return i;
        });
    }

    @Override
    public void addListener(final Runnable runnable) {
        listeners.add(runnable);
    }

    private record Relation(Set<Identifier> before, Set<Identifier> after) {
    }
}
