package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import net.minecraft.entity.Entity;

import java.util.function.BinaryOperator;

public class DebugPlayerBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<DebugPlayerBattleEntityComponent> CODEC = Codec.unit(DebugPlayerBattleEntityComponent::new);
    public static final BinaryOperator<DebugPlayerBattleEntityComponent> COMBINER = (debugPlayerBattleEntityComponent, debugPlayerBattleEntityComponent2) -> {
        throw new UnsupportedOperationException("Cannot combine components!");
    };

    @Override
    public void applyToState(final BattleParticipantState state) {
    }

    @Override
    public void applyToEntity(final Entity entity, final BattleView view) {

    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return null;
    }
}
