package io.github.stuff_stuffs.tbcexv3core.api.battles.action;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.Identifier;

import java.util.*;

public class InitialTeamSetupBattleAction implements BattleAction {
    public static final Codec<InitialTeamSetupBattleAction> CODEC = Codec.list(Entry.CODEC).xmap(InitialTeamSetupBattleAction::new, action -> action.entries);
    private final List<Entry> entries;

    private InitialTeamSetupBattleAction(final List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public BattleActionType<?> getType() {
        return CoreBattleActions.INITIAL_TEAM_SETUP_ACTION;
    }

    @Override
    public Optional<BattleParticipantHandle> getActor() {
        return Optional.empty();
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
        final BattleStatePhase phase = state.getPhase();
        if (phase != BattleStatePhase.INITIALIZATION) {
            throw new TBCExException("Tried to set up initial teams while not in initialization phase!");
        }
        final Map<Identifier, BattleParticipantTeam> teamMap = new Object2ReferenceOpenHashMap<>();
        for (final Entry entry : entries) {
            teamMap.put(entry.team(), state.addTeam(entry.team()));
        }
        for (final Entry entry : entries) {
            final BattleParticipantTeam team = teamMap.get(entry.team());
            for (final Identifier ally : entry.allies()) {
                if (!state.setTeamRelation(team, teamMap.get(ally), BattleParticipantTeamRelation.ALLIES, trace)) {
                    throw new TBCExException("Error while setting up initial teams!");
                }
            }
            for (final Identifier enemy : entry.enemies()) {
                if (!state.setTeamRelation(team, teamMap.get(enemy), BattleParticipantTeamRelation.ENEMIES, trace)) {
                    throw new TBCExException("Error while setting up initial teams!");
                }
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private record Entry(Identifier team, List<Identifier> allies, List<Identifier> enemies) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(Identifier.CODEC.fieldOf("team").forGetter(Entry::team), Codec.list(Identifier.CODEC).fieldOf("allies").forGetter(Entry::allies), Codec.list(Identifier.CODEC).fieldOf("enemies").forGetter(Entry::enemies)).apply(instance, Entry::new));
    }

    public static final class Builder {
        private final Set<Identifier> teams;
        private final Map<Pair<Identifier, Identifier>, BattleParticipantTeamRelation> relations;

        private Builder() {
            teams = new ObjectOpenHashSet<>();
            relations = new Object2ReferenceOpenHashMap<>();
        }

        public Builder addTeam(final Identifier team) {
            teams.add(team);
            return this;
        }

        public Builder setRelation(final Identifier first, final Identifier second, final BattleParticipantTeamRelation relation) {
            if (first.equals(second)) {
                throw new IllegalArgumentException("Tried to set a team relation to itself");
            }
            if (!teams.contains(first) || !teams.contains(second)) {
                throw new IllegalArgumentException("Unknown team added to builder!");
            }
            if (first.compareTo(second) < 0) {
                relations.put(Pair.of(first, second), relation);
            } else {
                relations.put(Pair.of(second, first), relation);
            }
            return this;
        }

        public InitialTeamSetupBattleAction build() {
            final Map<Identifier, Pair<List<Identifier>, List<Identifier>>> partial = new Object2ReferenceOpenHashMap<>();
            for (final Identifier team : teams) {
                partial.put(team, Pair.of(new ArrayList<>(), new ArrayList<>()));
            }
            for (final Map.Entry<Pair<Identifier, Identifier>, BattleParticipantTeamRelation> entry : relations.entrySet()) {
                final Pair<List<Identifier>, List<Identifier>> first = partial.get(entry.getKey().getFirst());
                final Pair<List<Identifier>, List<Identifier>> second = partial.get(entry.getKey().getSecond());
                (entry.getValue() == BattleParticipantTeamRelation.ALLIES ? first.getFirst() : first.getSecond()).add(entry.getKey().getSecond());
                (entry.getValue() == BattleParticipantTeamRelation.ALLIES ? second.getFirst() : second.getSecond()).add(entry.getKey().getFirst());
            }
            final List<Entry> entries = new ArrayList<>(partial.size());
            for (final Map.Entry<Identifier, Pair<List<Identifier>, List<Identifier>>> entry : partial.entrySet()) {
                entries.add(new Entry(entry.getKey(), entry.getValue().getFirst(), entry.getValue().getSecond()));
            }
            return new InitialTeamSetupBattleAction(entries);
        }
    }
}
