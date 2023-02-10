package io.github.stuff_stuffs.tbcexv3_test.common.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleActionType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;

import java.util.Optional;

public class BattleParticipantMeleeTestBattleAction implements BattleAction {
    public static final Codec<BattleParticipantMeleeTestBattleAction> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BattleParticipantHandle.codec().fieldOf("attacker").forGetter(action -> action.attacker),
            BattleParticipantHandle.codec().fieldOf("target").forGetter(action -> action.target)
    ).apply(instance, BattleParticipantMeleeTestBattleAction::new));
    private static final float DAMAGE = 5.0F;
    private final BattleParticipantHandle attacker;
    private final BattleParticipantHandle target;

    public BattleParticipantMeleeTestBattleAction(final BattleParticipantHandle attacker, final BattleParticipantHandle target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public BattleActionType<?> getType() {
        return TestBattleActions.MELEE_TEST_BATTLE_ACTION;
    }

    @Override
    public Optional<BattleParticipantHandle> getActor() {
        return Optional.of(attacker);
    }

    @Override
    public void apply(final BattleState state, final Tracer<ActionTrace> trace) {
        final BattleParticipantState attackerState = state.getParticipantByHandle(attacker);
        final BattleParticipantState targetState = state.getParticipantByHandle(target);
        if (state.getTeamRelation(attackerState.getTeam(), targetState.getTeam()) != BattleParticipantTeamRelation.ENEMIES) {
            throw new RuntimeException();
        }
        targetState.setHealth(targetState.getHealth() - DAMAGE, trace);
    }
}
