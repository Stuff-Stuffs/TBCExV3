package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.stat.BattleParticipantStat;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.stat.BattleParticipantStatMap;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.stat.BattleParticipantStatModifierKey;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.stat.StatTrace;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import it.unimi.dsi.fastutil.ints.Int2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceSortedMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;

import java.util.Map;

public class BattleParticipantStatMapImpl implements BattleParticipantStatMap {
    private final Map<BattleParticipantStat, Entry> entries;

    public BattleParticipantStatMapImpl() {
        entries = new Reference2ObjectOpenHashMap<>();
    }

    @Override
    public BattleParticipantStatModifierKey addModifier(final BattleParticipantStat stat, final Modifier modifier) {
        return entries.computeIfAbsent(stat, i -> new Entry()).addModifier(modifier);
    }

    @Override
    public double compute(final BattleParticipantStat stat, final Tracer<StatTrace> tracer) {
        return entries.computeIfAbsent(stat, i -> new Entry()).compute(tracer);
    }

    private static final class Entry {
        private final Int2ReferenceSortedMap<Bucket> buckets;

        private Entry() {
            buckets = new Int2ReferenceAVLTreeMap<>();
        }

        public double compute(final Tracer<StatTrace> tracer) {
            double val = 0;
            for (final Bucket bucket : buckets.values()) {
                val = bucket.compute(val, tracer);
            }
            return val;
        }

        public KeyImpl addModifier(final Modifier modifier) {
            final Bucket bucket = buckets.computeIfAbsent(modifier.getPriority(), i -> new Bucket());
            final KeyImpl key = new KeyImpl(bucket);
            bucket.modifiers.putAndMoveToLast(key, modifier);
            return key;
        }
    }

    private static final class Bucket {
        private final Reference2ReferenceLinkedOpenHashMap<KeyImpl, Modifier> modifiers = new Reference2ReferenceLinkedOpenHashMap<>();

        public double compute(double value, final Tracer<StatTrace> tracer) {
            for (final Modifier modifier : modifiers.values()) {
                value = modifier.modify(value, tracer);
            }
            return value;
        }

        public void destroy(final KeyImpl key) {
            modifiers.remove(key);
        }
    }

    private static final class KeyImpl implements BattleParticipantStatModifierKey {
        private final Bucket bucket;
        private boolean alive;

        public KeyImpl(final Bucket bucket) {
            this.bucket = bucket;
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
            bucket.destroy(this);
            alive = false;
        }
    }
}
