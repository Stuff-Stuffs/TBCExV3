package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.*;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.TopologicalSort;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BattleParticipantStatMapImpl implements BattleParticipantStatMap {
    private final Map<BattleParticipantStat, Entry> entries;

    public BattleParticipantStatMapImpl() {
        entries = new Reference2ObjectOpenHashMap<>();
    }

    @Override
    public BattleParticipantStatModifierKey addModifier(final BattleParticipantStat stat, final BattleParticipantStatModifier modifier, final Tracer<ActionTrace> tracer) {
        return entries.computeIfAbsent(stat, i -> new Entry()).addModifier(modifier);
    }

    @Override
    public double compute(final BattleParticipantStat stat, final Tracer<StatTrace> tracer) {
        return entries.computeIfAbsent(stat, i -> new Entry()).compute(tracer);
    }

    private static final class Entry {
        private final List<WrappedModifier> modifiers;
        private List<WrappedModifier> sorted = List.of();

        private Entry() {
            modifiers = new ArrayList<>();
        }

        public double compute(final Tracer<StatTrace> tracer) {
            double val = 0;
            for (final WrappedModifier modifier : sorted) {
                val = modifier.modify(val, tracer);
            }
            return val;
        }

        public KeyImpl addModifier(final BattleParticipantStatModifier modifier) {
            final int index = modifiers.size();
            final WrappedModifier wrappedModifier = new WrappedModifier(index, modifier);
            modifiers.add(wrappedModifier);
            sort();
            return new KeyImpl(wrappedModifier, this);
        }

        public void remove(final WrappedModifier modifier, final Tracer<ActionTrace> tracer) {
            modifiers.remove(modifier);
            sort();
        }

        private void sort() {
            sorted = TopologicalSort.tieBreakingSort(modifiers, (parent, child, items) -> {
                final BattleParticipantStatModifierPhase possibleParent = items.get(parent).getPhase();
                final BattleParticipantStatModifierPhase possibleChild = items.get(child).getPhase();
                return possibleParent.getHappensBefore().contains(possibleChild.getId()) || possibleChild.getHappensAfter().contains(possibleParent.getId());
            }, Comparator.comparingInt(wrapped -> wrapped.index));
        }
    }

    private static final class WrappedModifier implements BattleParticipantStatModifier {
        private final int index;
        private final BattleParticipantStatModifierPhase phase;
        private final BattleParticipantStatModifier wrapped;

        private WrappedModifier(final int index, final BattleParticipantStatModifier wrapped) {
            this.index = index;
            phase = wrapped.getPhase();
            this.wrapped = wrapped;
        }

        @Override
        public BattleParticipantStatModifierPhase getPhase() {
            return phase;
        }

        @Override
        public double modify(final double value, final Tracer<StatTrace> tracer) {
            return wrapped.modify(value, tracer);
        }
    }

    private static final class KeyImpl implements BattleParticipantStatModifierKey {
        private final WrappedModifier modifier;
        private final Entry entry;
        private boolean alive;

        public KeyImpl(final WrappedModifier modifier, final Entry entry) {
            this.modifier = modifier;
            this.entry = entry;
            alive = true;
        }

        @Override
        public boolean isDestroyed() {
            return alive;
        }

        @Override
        public void destroy(final Tracer<ActionTrace> tracer) {
            if (isDestroyed()) {
                throw new TBCExException();
            }
            entry.remove(modifier, tracer);
            alive = false;
        }
    }
}
