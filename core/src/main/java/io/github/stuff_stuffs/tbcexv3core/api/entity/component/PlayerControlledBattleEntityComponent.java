package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.Codecs;

import java.util.UUID;
import java.util.function.BinaryOperator;

public class PlayerControlledBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<PlayerControlledBattleEntityComponent> CODEC = Codecs.UUID.xmap(PlayerControlledBattleEntityComponent::new, component -> component.controllerUUID);
    public static final BinaryOperator<PlayerControlledBattleEntityComponent> COMBINER = (playerControlledBattleEntityComponent, playerControlledBattleEntityComponent2) -> {
        throw new UnsupportedOperationException("Cannot combine flag like component");
    };
    private final UUID controllerUUID;

    public PlayerControlledBattleEntityComponent(final UUID controllerUUID) {
        this.controllerUUID = controllerUUID;
    }

    @Override
    public void applyToState(final BattleParticipantState state) {

    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {

    }

    @Override
    public void applyToEntityOnJoin(final BattleParticipantHandle handle, final Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld world)) {
            throw new TBCExException("Tried to apply component client side!");
        }
        final Entity maybePlayer = world.getEntity(controllerUUID);
        if (!(maybePlayer instanceof PlayerEntity player)) {
            throw new TBCExException("Gave non-player uuid to player controlled component");
        }
        ((TBCExPlayerEntity) player).tbcex$setCurrentBattle(handle.getParent());
    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.PLAYER_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
