package io.github.stuff_stuffs.tbcexv3core.api.battles.tracer;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ReconstructedTeamMap {
    private final Map<Identifier, Entry> entries;
    private final Map<BattleParticipantHandle, Identifier> byHandle;

    private ReconstructedTeamMap(final Map<Identifier, Entry> entries, final Map<BattleParticipantHandle, Identifier> handle) {
        this.entries = entries;
        byHandle = handle;
    }

    public Entry getTeamEntry(final Identifier id) {
        return entries.get(id);
    }

    public Identifier getTeam(final BattleParticipantHandle handle) {
        return byHandle.get(handle);
    }

    public static final class Entry {
        private final Set<BattleParticipantHandle> participants;
        private final Map<Identifier, BattleParticipantTeamRelation> relations;

        private Entry(final Set<BattleParticipantHandle> participants, final Map<Identifier, BattleParticipantTeamRelation> relations) {
            this.participants = participants;
            this.relations = relations;
        }

        public Set<BattleParticipantHandle> getParticipants() {
            return Collections.unmodifiableSet(participants);
        }

        public BattleParticipantTeamRelation getRelation(final Identifier team) {
            return relations.getOrDefault(team, BattleParticipantTeamRelation.NEUTRAL);
        }
    }

    public static final class Builder {
        private final Map<BattleParticipantHandle, Identifier> byHandle = new Object2ObjectOpenHashMap<>();
        private final Map<Pair<Identifier, Identifier>, BattleParticipantTeamRelation> relations = new Object2ObjectOpenHashMap<>();


        public Builder setRelation(final Identifier first, final Identifier second, final BattleParticipantTeamRelation relation) {
            if (first.equals(second)) {
                return this;
            }
            if (first.compareTo(second) < 0) {
                relations.put(Pair.of(first, second), relation);
            } else {
                relations.put(Pair.of(second, first), relation);
            }
            return this;
        }

        public Builder setTeam(final BattleParticipantHandle handle, final Identifier team) {
            byHandle.put(handle, team);
            return this;
        }

        public Builder removeParticipant(final BattleParticipantHandle handle) {
            byHandle.remove(handle);
            return this;
        }

        public ReconstructedTeamMap build() {
            Map<Identifier, Set<BattleParticipantHandle>> teamSet = new Object2ObjectOpenHashMap<>();
            for (Map.Entry<BattleParticipantHandle, Identifier> entry : byHandle.entrySet()) {
                teamSet.computeIfAbsent(entry.getValue(), i -> new ObjectOpenHashSet<>()).add(entry.getKey());
            }
            Map<Identifier, Entry> entries = new Object2ObjectOpenHashMap<>();
            for (Map.Entry<Identifier, Set<BattleParticipantHandle>> entry : teamSet.entrySet()) {
                if(entry.getValue().isEmpty()) {
                    continue;
                }
                Map<Identifier, BattleParticipantTeamRelation> relations = new Object2ObjectOpenHashMap<>();
                for (Map.Entry<Pair<Identifier, Identifier>, BattleParticipantTeamRelation> relationEntry : this.relations.entrySet()) {
                    final Pair<Identifier, Identifier> key = relationEntry.getKey();
                    if(key.getFirst().equals(entry.getKey())) {
                        relations.put(key.getSecond(), relationEntry.getValue());
                    } else if(key.getSecond().equals(entry.getKey())) {
                        relations.put(key.getFirst(), relationEntry.getValue());
                    }
                }
                entries.put(entry.getKey(), new Entry(entry.getValue(), relations));
            }
            return new ReconstructedTeamMap(entries, new Object2ObjectOpenHashMap<>(byHandle));
        }
    }
}
