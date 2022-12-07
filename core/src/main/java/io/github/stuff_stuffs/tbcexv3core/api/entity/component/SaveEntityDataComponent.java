package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BinaryOperator;

public class SaveEntityDataComponent implements BattleEntityComponent {
    public static final Codec<SaveEntityDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.UUID.fieldOf("id").forGetter(component -> component.uuid), CodecUtil.conversionCodec(NbtOps.INSTANCE).fieldOf("entityData").forGetter(component -> component.entityData)).apply(instance, SaveEntityDataComponent::new));
    public static final BinaryOperator<SaveEntityDataComponent> COMBINER = (first, second) -> {
        throw new UnsupportedOperationException("Cannot combine entity data components!");
    };
    private final UUID uuid;
    private final NbtCompound entityData;

    public SaveEntityDataComponent(final UUID uuid, final NbtCompound entityData) {
        this.uuid = uuid;
        this.entityData = entityData;
    }

    private SaveEntityDataComponent(final UUID uuid, final NbtElement entityData) {
        this.uuid = uuid;
        this.entityData = (NbtCompound) entityData;
    }

    public SaveEntityDataComponent() {
        uuid = Util.NIL_UUID;
        entityData = new NbtCompound();
    }

    @Override
    public void applyToState(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {

    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {
        Entity entity = world.getEntity(uuid);
        if (entity == null) {
            final Optional<Entity> optionalEntity = EntityType.getEntityFromNbt(entityData, world);
            if (optionalEntity.isEmpty()) {
                throw new TBCExException("Error while respawning entity into world!");
            }
            if (optionalEntity.get().getType() != EntityType.PLAYER) {
                entity = optionalEntity.get();
                world.spawnEntity(entity);
            } else {
                ((ServerBattleWorld) world).pushDelayedPlayerComponent(uuid, view.getState().getHandle(), this);
            }
        } else {
            final Optional<EntityType<?>> type = EntityType.fromNbt(entityData);
            if (type.isEmpty()) {
                throw new TBCExException("Error while respawning entity into world, could not get type!");
            }
            if (type.get() != entity.getType()) {
                throw new TBCExException("Entity type mismatch while loading from battle!");
            }
            entity.readNbt(entityData);
        }
    }

    @Override
    public void applyToEntityOnJoin(final BattleParticipantHandle handle, final Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            entity.remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.AI_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
