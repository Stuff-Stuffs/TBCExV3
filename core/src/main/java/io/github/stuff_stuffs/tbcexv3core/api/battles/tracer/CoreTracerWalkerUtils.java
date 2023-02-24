package io.github.stuff_stuffs.tbcexv3core.api.battles.tracer;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleActionTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleParticipantActionTraces;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerView;
import io.github.stuff_stuffs.tbcexv3util.api.util.TracerWalker;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

public final class CoreTracerWalkerUtils {
    public static TracerWalker<ActionTrace, ReconstructedTeamMap.Builder> teams() {
        final TracerWalker.Builder<ActionTrace, ReconstructedTeamMap.Builder> builder = TracerWalker.builder();
        builder.add(i -> i.value() instanceof BattleParticipantActionTraces.BattleParticipantSetTeam || i.value() instanceof BattleActionTraces.BattleTeamSetRelation, (traceNode, builder1) -> {
            if (traceNode.value() instanceof BattleParticipantActionTraces.BattleParticipantSetTeam setTeam) {
                builder1.setTeam(setTeam.handle(), setTeam.newTeam().getIdentifier());
            } else if (traceNode.value() instanceof BattleActionTraces.BattleTeamSetRelation setRelation) {
                builder1.setRelation(setRelation.first().getIdentifier(), setRelation.second().getIdentifier(), setRelation.newRelation());
            }
            return builder1;
        });
        return builder.build(new ReconstructedTeamMap.Builder());
    }

    public static void walkBackwards(final TracerView.Node<ActionTrace> node, final TracerWalker<ActionTrace, ?> walker) {
        final Set<TracerView.Node<ActionTrace>> encountered = new ObjectOpenHashSet<>();
        final Queue<TracerView.Node<ActionTrace>> traces = new ArrayDeque<>();
        traces.add(node);
        TracerView.Node<ActionTrace> cursor = null;
        while ((cursor = traces.poll()) != null) {
            for (final TracerView.Node<ActionTrace> next : cursor.relations().get(TracerView.CAUSED_BY)) {
                if (encountered.add(next)) {
                    walker.accept(cursor);
                    traces.add(next);
                }
            }
        }
    }

    private CoreTracerWalkerUtils() {
    }
}
