package io.github.stuff_stuffs.tbcexv3_test.common.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3_test.common.CreativeModeBattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatModifier;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatModifierPhase;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.CoreBattleParticipantStats;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.StatTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.function.BinaryOperator;

public class TestEntityComponent implements BattleEntityComponent {
    public static final Codec<TestEntityComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.DOUBLE.fieldOf("currentHealth").forGetter(component -> component.currentHealth), Codec.DOUBLE.fieldOf("maxHealth").forGetter(component -> component.maxHealth), Codec.BOOL.fieldOf("creative").forGetter(component -> component.creative)).apply(instance, TestEntityComponent::new));
    public static final BinaryOperator<TestEntityComponent> COMBINER = (first, second) -> {
        throw new UnsupportedOperationException("Cannot combine TestEntityComponents!");
    };
    private final double currentHealth;
    private final double maxHealth;
    private final boolean creative;

    public TestEntityComponent(final double currentHealth, final double maxHealth, final boolean creative) {
        this.creative = creative;
        this.currentHealth = Math.min(currentHealth, maxHealth);
        this.maxHealth = maxHealth;
    }

    @Override
    public void applyToState(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {
        state.getStatMap().addModifier(CoreBattleParticipantStats.MAX_HEALTH, new BattleParticipantStatModifier() {
            @Override
            public BattleParticipantStatModifierPhase getPhase() {
                return BattleParticipantStatModifierPhase.ADD;
            }

            @Override
            public double modify(final double value, @Nullable final Tracer<StatTrace> tracer) {
                return value + maxHealth;
            }
        }, tracer);
        state.setHealth(currentHealth, tracer);
        if (creative) {
            state.addEffect(new CreativeModeBattleParticipantEffect(), tracer);
            state.setHealth(100000000, tracer);
        }
    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {

    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return TestEntities.TEST_ENTITY_COMPONENT_TYPE;
    }
}
