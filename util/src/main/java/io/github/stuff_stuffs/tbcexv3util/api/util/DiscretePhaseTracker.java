package io.github.stuff_stuffs.tbcexv3util.api.util;

import io.github.stuff_stuffs.tbcexv3util.impl.util.DiscretePhaseTrackerImpl;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.Set;

public interface DiscretePhaseTracker {
    Set<Identifier> phases();

    void addPhase(Identifier identifier, Set<Identifier> phasesBefore, Set<Identifier> phasesAfter);

    void addRelation(Identifier identifier, Set<Identifier> phasesBefore, Set<Identifier> phasesAfter);

    Comparator<Identifier> phaseComparator();

    static DiscretePhaseTracker create() {
        return new DiscretePhaseTrackerImpl();
    }
}
