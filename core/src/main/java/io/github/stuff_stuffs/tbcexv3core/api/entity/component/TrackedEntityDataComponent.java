package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.server.world.ServerWorld;

import java.util.function.BinaryOperator;

public class TrackedEntityDataComponent implements BattleEntityComponent {
    public static final TrackedEntityDataComponent INSTANCE = new TrackedEntityDataComponent();
    public static final Codec<TrackedEntityDataComponent> CODEC = Codec.unit(INSTANCE);
    public static final BinaryOperator<TrackedEntityDataComponent> COMBINER = (first, second) -> {
        throw new UnsupportedOperationException("Cannot combine flag like component");
    };

    private TrackedEntityDataComponent() {
    }

    @Override
    public void applyToState(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {

    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {

    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.TRACKED_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
