package io.github.stuff_stuffs.tbcexv3core.impl.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleParticipantActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.CoreBattleParticipantEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStatePhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.state.AbstractBattleParticipantState;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ParticipantContainer {
    private final Map<BattleParticipantHandle, AbstractBattleParticipantState> participantStates;
    private final Map<Identifier, BattleParticipantTeam> teams;
    private final Map<UnorderedTeamPair, BattleParticipantTeamRelation> relations;
    private final Map<BattleParticipantTeam, Set<BattleParticipantHandle>> handlesByTeam;
    private final Map<BattleParticipantHandle, BattleParticipantTeam> teamByHandle;
    private final EventMap events;

    public ParticipantContainer(final EventMap events) {
        this.events = events;
        participantStates = new Object2ReferenceOpenHashMap<>();
        teams = new Object2ReferenceOpenHashMap<>();
        relations = new Object2ReferenceOpenHashMap<>();
        handlesByTeam = new Object2ReferenceOpenHashMap<>();
        teamByHandle = new Object2ReferenceOpenHashMap<>();
    }

    public Iterable<BattleParticipantHandle> getParticipants() {
        return Collections.unmodifiableSet(participantStates.keySet());
    }

    public AbstractBattleParticipantState getParticipantByHandle(final BattleParticipantHandle handle) {
        final AbstractBattleParticipantState state = participantStates.get(handle);
        if (state == null) {
            throw new TBCExException();
        }
        return state;
    }

    public Optional<BattleParticipantTeam> getTeamById(final Identifier id) {
        return Optional.ofNullable(teams.get(id));
    }

    public BattleParticipantTeamRelation getTeamRelation(final BattleParticipantTeam first, final BattleParticipantTeam second) {
        if (first.equals(second)) {
            return BattleParticipantTeamRelation.ALLIES;
        }
        return relations.getOrDefault(new UnorderedTeamPair(first, second), BattleParticipantTeamRelation.NEUTRAL);
    }

    public Iterable<BattleParticipantHandle> getParticipantsByTeam(final BattleParticipantTeam team) {
        return Collections.unmodifiableCollection(handlesByTeam.getOrDefault(team, Collections.emptySet()));
    }

    public Optional<BattleParticipantHandle> addParticipant(final BattleParticipantState participant, final BattleParticipantTeam team, final Tracer<ActionTrace> tracer, final BattleHandle battleHandle) {
        if (participant.getPhase().getOrder() > BattleParticipantStatePhase.SETUP.getOrder()) {
            throw new TBCExException();
        }
        if (!(participant instanceof AbstractBattleParticipantState)) {
            throw new TBCExException();
        }
        final BattleParticipantHandle handle = BattleParticipantHandle.of(participant.getUuid(), battleHandle);
        if (participantStates.containsKey(handle)) {
            throw new TBCExException();
        }
        if (events.getEvent(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_JOIN_EVENT).getInvoker().preBattleParticipantJoin(participant, tracer)) {
            participantStates.put(handle, (AbstractBattleParticipantState) participant);
            teamByHandle.put(handle, team);
            handlesByTeam.computeIfAbsent(team, i -> new ObjectOpenHashSet<>()).add(handle);
            tracer.pushInstant(true).value(new BattleParticipantActionTraces.BattleParticipantJoined(handle)).buildAndApply();
            return Optional.of(handle);
        }
        return Optional.empty();
    }

    public boolean removeParticipant(final BattleParticipantHandle handle, final BattleParticipantRemovalReason reason, final Tracer<ActionTrace> tracer, final BattleState state) {
        if (!participantStates.containsKey(handle)) {
            throw new IllegalArgumentException("Tried to remove a non-existent battle participant!");
        }
        if (events.getEvent(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_LEAVE_EVENT).getInvoker().preParticipantLeaveEvent(handle, state, reason, tracer)) {
            final AbstractBattleParticipantState removed = participantStates.remove(handle);
            handlesByTeam.get(removed.getTeam()).remove(handle);
            removed.finish();
            tracer.pushInstant(true).value(new BattleParticipantActionTraces.BattleParticipantLeft(handle, reason)).buildAndApply();
            events.getEvent(CoreBattleEvents.POST_BATTLE_PARTICIPANT_LEAVE_EVENT).getInvoker().postParticipantLeaveEvent(removed, state, reason, tracer);
            tracer.pop();
            return true;
        }
        return false;
    }

    public boolean setTeamRelation(final BattleParticipantTeam first, final BattleParticipantTeam second, final BattleParticipantTeamRelation relation, final Tracer<ActionTrace> tracer, final BattleState state) {
        final UnorderedTeamPair pair = new UnorderedTeamPair(first, second);
        final BattleParticipantTeamRelation old = relations.getOrDefault(pair, BattleParticipantTeamRelation.NEUTRAL);
        if (old == relation) {
            return true;
        }
        if (events.getEvent(CoreBattleEvents.PRE_TEAM_RELATION_CHANGE_EVENT).getInvoker().preChangeTeamRelation(state, first, second, old, relation, tracer)) {
            if (relation == BattleParticipantTeamRelation.NEUTRAL) {
                relations.remove(pair);
            } else {
                relations.put(pair, relation);
            }
            tracer.pushInstant(true).value(new BattleActionTraces.BattleTeamSetRelation(first, second, old, relation)).buildAndApply();
            events.getEvent(CoreBattleEvents.POST_TEAM_RELATION_CHANGE_EVENT).getInvoker().postChangeTeamRelation(state, first, second, old, relation, tracer);
            tracer.pop();
            return true;
        }
        return false;
    }

    public boolean canEnd() {
        for (final BattleParticipantTeam first : teams.values()) {
            if(handlesByTeam.get(first).isEmpty()) {
                continue;
            }
            for (final BattleParticipantTeam second : teams.values()) {
                if(handlesByTeam.get(second).isEmpty()) {
                    continue;
                }
                if (first == second) {
                    continue;
                }
                if (relations.getOrDefault(new UnorderedTeamPair(first, second), BattleParticipantTeamRelation.NEUTRAL) == BattleParticipantTeamRelation.ENEMIES) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isEmpty() {
        return participantStates.isEmpty();
    }

    public boolean setTeam(final BattleParticipantHandle handle, final BattleParticipantTeam team, final Tracer<ActionTrace> tracer) {
        final BattleParticipantTeam currentTeam = teamByHandle.get(handle);
        if (currentTeam == null) {
            throw new TBCExException("Tried to set unknown participants team!");
        }
        if (currentTeam.equals(team)) {
            return true;
        }
        final AbstractBattleParticipantState state = participantStates.get(handle);
        if (state.getEventMap().getEvent(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_TEAM_EVENT).getInvoker().preSetTeam(state, team, tracer)) {
            handlesByTeam.get(currentTeam).remove(handle);
            handlesByTeam.computeIfAbsent(team, i -> new ObjectOpenHashSet<>()).add(handle);
            teamByHandle.put(handle, team);
            tracer.pushInstant(true).value(new BattleParticipantActionTraces.BattleParticipantSetTeam(handle, team, currentTeam)).buildAndApply();
            state.getEventMap().getEvent(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_TEAM_EVENT).getInvoker().postSetTeam(state, currentTeam, tracer);
            tracer.pop();
            return true;
        }
        return false;
    }

    public BattleParticipantTeam getTeam(final BattleParticipantHandle handle) {
        return teamByHandle.get(handle);
    }

    public BattleParticipantTeam addTeam(final Identifier identifier, final BattleHandle handle) {
        BattleParticipantTeam team = teams.get(identifier);
        if (team != null) {
            throw new IllegalStateException("Team already exists");
        }
        team = BattleParticipantTeam.of(handle, identifier);
        teams.put(identifier, team);
        return team;
    }

    public boolean removeTeam(final BattleParticipantTeam team) {
        final Set<BattleParticipantHandle> handles = handlesByTeam.get(team);
        if (handles == null || handles.isEmpty()) {
            handlesByTeam.remove(team);
            return true;
        }
        return false;
    }

    public Stream<BattleParticipantHandle> getParticipantStream() {
        return participantStates.keySet().stream();
    }

    private record UnorderedTeamPair(BattleParticipantTeam first, BattleParticipantTeam second) {
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UnorderedTeamPair that)) {
                return false;
            }

            return (first.equals(that.first) && second.equals(that.second)) || (first.equals(that.second) && second.equals(that.first));
        }

        @Override
        public int hashCode() {
            return first.hashCode() ^ second.hashCode();
        }
    }
}
