package io.github.stuff_stuffs.tbcexv3_test.common;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3_test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.*;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CreativeModeBattleParticipantEffect implements BattleParticipantEffect {
    public static final Codec<CreativeModeBattleParticipantEffect> CODEC = Codec.unit(new CreativeModeBattleParticipantEffect());
    private BattleParticipantStatModifierKey key;

    @Override
    public void init(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {
        key = state.getStatMap().addModifier(CoreBattleParticipantStats.MAX_HEALTH, new BattleParticipantStatModifier() {
            @Override
            public BattleParticipantStatModifierPhase getPhase() {
                return BattleParticipantStatModifierPhase.ADD;
            }

            @Override
            public double modify(final double value, @Nullable final Tracer<StatTrace> tracer) {
                return value + 1000000;
            }
        }, tracer);
    }

    @Override
    public void deinit(final Tracer<ActionTrace> tracer) {
        key.destroy(tracer);
    }

    @Override
    public BattleParticipantEffectType<?, ?> getType() {
        return TestEntities.CREATIVE_MODE_EFFECT;
    }

    @Override
    public List<BattleParticipantAction> getActions() {
        return List.of();
    }
}
