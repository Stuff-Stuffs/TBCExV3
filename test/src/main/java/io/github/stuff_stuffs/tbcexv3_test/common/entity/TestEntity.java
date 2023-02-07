package io.github.stuff_stuffs.tbcexv3_test.common.entity;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.SaveEntityDataComponent;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Collections;

public class TestEntity extends LivingEntity implements BattleEntity {
    public TestEntity(final EntityType<? extends LivingEntity> entityType, final World world) {
        super(entityType, world);
    }

    @Override
    public BattleParticipantBounds getDefaultBounds() {
        final Box box = getDimensions(EntityPose.STANDING).getBoxAt(Vec3d.ofBottomCenter(getBlockPos()));
        return BattleParticipantBounds.builder(getBlockPos()).add(TBCExV3Core.createId("body"), box).build();
    }

    @Override
    public void buildParticipantState(final BattleParticipantStateBuilder builder) {
        final NbtCompound compound = new NbtCompound();
        writeNbt(compound);
        final EntityType<?> entityType = getType();
        final Identifier identifier = EntityType.getId(entityType);
        compound.putString("id", identifier.toString());
        builder.addComponent(new SaveEntityDataComponent(getUuid(), compound));
        builder.addComponent(new TestEntityComponent(20, 20));
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return Collections.emptyList();
    }

    @Override
    public ItemStack getEquippedStack(final EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(final EquipmentSlot slot, final ItemStack stack) {
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }
}
