package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import net.minecraft.server.world.ServerWorld;

import java.util.function.BinaryOperator;

public class DebugBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<DebugBattleEntityComponent> CODEC = Codec.unit(DebugBattleEntityComponent::new);
    public static final BinaryOperator<DebugBattleEntityComponent> COMBINER = (debugBattleEntityComponent, debugBattleEntityComponent2) -> {
        throw new UnsupportedOperationException("Cannot combine components!");
    };

    @Override
    public void applyToState(final BattleParticipantState state) {
    }

    @Override
    public void onLeave(BattleView view, ServerWorld world) {
        
    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.DEBUG_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
