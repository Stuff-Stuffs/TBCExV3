package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3core.internal.common.world.BattleDisplayWorld;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.UUID;
import java.util.function.BinaryOperator;

public class PlayerControlledBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<PlayerControlledBattleEntityComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CodecUtil.UUID_CODEC.fieldOf("controller").forGetter(component -> component.controllerUUID),
            RegistryKey.createCodec(RegistryKeys.WORLD).fieldOf("startDim").forGetter(component -> component.startDimension),
            BlockPos.CODEC.fieldOf("startPos").forGetter(component -> component.startPos)
    ).apply(instance, PlayerControlledBattleEntityComponent::new));
    public static final BinaryOperator<PlayerControlledBattleEntityComponent> COMBINER = (playerControlledBattleEntityComponent, playerControlledBattleEntityComponent2) -> {
        throw new UnsupportedOperationException("Cannot combine flag like component");
    };
    private final UUID controllerUUID;
    private final RegistryKey<World> startDimension;
    private final BlockPos startPos;

    public PlayerControlledBattleEntityComponent(final UUID controllerUUID, final RegistryKey<World> dimension, final BlockPos pos) {
        this.controllerUUID = controllerUUID;
        startDimension = dimension;
        startPos = pos;
    }

    @Override
    public void applyToState(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {

    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {
        final Entity entity = world.getServer().getWorld(BattleDisplayWorld.BATTLE_DISPLAY_WORLD).getEntity(controllerUUID);
        if (entity != null) {
            final ServerWorld startWorld = world.getServer().getWorld(startDimension);
            FabricDimensions.teleport(entity, startWorld != null ? startWorld : world.getServer().getOverworld(), new TeleportTarget(startPos.toCenterPos(), Vec3d.ZERO, 0, 0));
        }
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
        final MinecraftServer server = world.getServer();
        final BattleView battleView = ((BattleWorld) server.getWorld(handle.getParent().getWorldKey())).tryGetBattleView(handle.getParent());
        final BlockPos center = battleView.getState().getParticipantByHandle(handle).getBounds().center();
        FabricDimensions.teleport(player, server.getWorld(BattleDisplayWorld.BATTLE_DISPLAY_WORLD), new TeleportTarget(battleView.toGlobal(center.toCenterPos()), Vec3d.ZERO, 0, 0));
    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.PLAYER_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
