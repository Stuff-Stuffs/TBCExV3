package io.github.stuff_stuffs.tbcexv3test.common;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.BattleParticipantAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatModifier;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatModifierKey;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatModifierPhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.CoreBattleParticipantStats;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3test.common.entity.TestEntities;
import io.github.stuff_stuffs.tbcexv3util.api.util.OperationChainDisplayBuilder;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.OptionalDouble;

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
            public double modify(final double value, @Nullable final OperationChainDisplayBuilder displayBuilder) {
                if (displayBuilder != null) {
                    displayBuilder.push(OperationChainDisplayBuilder.Operation.ADD, OptionalDouble.of(1000000.0), Text.empty());
                }
                return value + 1000000.0;
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
