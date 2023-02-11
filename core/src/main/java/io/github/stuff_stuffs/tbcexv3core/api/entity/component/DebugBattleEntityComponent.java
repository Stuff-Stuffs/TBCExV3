package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.server.world.ServerWorld;

import java.util.function.BinaryOperator;

public class DebugBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<DebugBattleEntityComponent> CODEC = Codec.unit(DebugBattleEntityComponent::new);
    public static final BinaryOperator<DebugBattleEntityComponent> COMBINER = (debugBattleEntityComponent, debugBattleEntityComponent2) -> {
        throw new UnsupportedOperationException("Cannot combine flag like components!");
    };

    @Override
    public void applyToState(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {

    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {

    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.DEBUG_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
