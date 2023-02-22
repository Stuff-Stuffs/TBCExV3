package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.function.BinaryOperator;

public class AnimationDataBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<AnimationDataBattleEntityComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(Identifier.CODEC.fieldOf("modelTypeId").forGetter(AnimationDataBattleEntityComponent::getModelTypeId)).apply(instance, AnimationDataBattleEntityComponent::new));
    public static final BinaryOperator<AnimationDataBattleEntityComponent> COMBINER = (first, second) -> {
        throw new UnsupportedOperationException("Cannot combine animation data components!");
    };
    private final Identifier modelTypeId;

    public AnimationDataBattleEntityComponent(final Identifier id) {
        modelTypeId = id;
    }

    public Identifier getModelTypeId() {
        return modelTypeId;
    }

    @Override
    public void applyToState(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {

    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {

    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.ANIMATION_DATA_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
