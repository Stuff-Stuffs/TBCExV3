package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.dynamic.Codecs;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BinaryOperator;

public class AIControlledBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<AIControlledBattleEntityComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.UUID.fieldOf("id").forGetter(component -> component.uuid), CodecUtil.conversionCodec(NbtOps.INSTANCE).fieldOf("entityData").forGetter(component -> component.entityData)).apply(instance, AIControlledBattleEntityComponent::new));
    public static final BinaryOperator<AIControlledBattleEntityComponent> COMBINER = (first, second) -> {
        throw new UnsupportedOperationException("Cannot combine entity data components!");
    };
    private final UUID uuid;
    private final NbtCompound entityData;

    public AIControlledBattleEntityComponent(final UUID uuid, final NbtCompound entityData) {
        this.uuid = uuid;
        this.entityData = entityData;
    }

    private AIControlledBattleEntityComponent(final UUID uuid, final NbtElement entityData) {
        this.uuid = uuid;
        this.entityData = (NbtCompound) entityData;
    }

    @Override
    public void applyToState(final BattleParticipantState state) {

    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {
        Entity entity = world.getEntity(uuid);
        if (entity == null) {
            final Optional<Entity> optionalEntity = EntityType.getEntityFromNbt(entityData, world);
            if (optionalEntity.isEmpty()) {
                throw new TBCExException("Error while respawning entity into world!");
            }
            entity = optionalEntity.get();
            world.spawnEntity(entity);
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
    public void applyToEntityOnJoin(final Entity entity) {
        entity.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.AI_CONTROLLED_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
