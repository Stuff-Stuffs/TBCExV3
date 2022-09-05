package io.github.stuff_stuffs.tbcexv3core.api.entity;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import net.minecraft.entity.Entity;

public class PlayerControlledBattleEntityComponent implements BattleEntityComponent {
    @Override
    public void applyToState(BattleParticipantState state) {
        
    }

    @Override
    public void applyToEntity(Entity entity, BattleView view) {

    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return null;
    }
}
