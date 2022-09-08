package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import net.minecraft.server.world.ServerWorld;

import java.util.function.BinaryOperator;

public class PlayerControlledBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<PlayerControlledBattleEntityComponent> CODEC = Codec.unit(new PlayerControlledBattleEntityComponent());
    public static final BinaryOperator<PlayerControlledBattleEntityComponent> COMBINER = (playerControlledBattleEntityComponent, playerControlledBattleEntityComponent2) -> {
        throw new UnsupportedOperationException("Cannot combine flag like component");
    };

    @Override
    public void applyToState(final BattleParticipantState state) {

    }

    @Override
    public void onLeave(BattleView view, ServerWorld world) {
        
    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.PLAYER_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
