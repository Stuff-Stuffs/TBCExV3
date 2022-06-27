package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.inventory.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.equipment.BattleParticipantEquipmentSlotImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantEquipmentSlot {
    Registry<BattleParticipantEquipmentSlot> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.ofRegistry(TBCExV3Core.createId("battle_participant_equipment_slots")), Lifecycle.stable(), BattleParticipantEquipmentSlot::getReference)).buildAndRegister();
    Codec<BattleParticipantEquipmentSlot> CODEC = REGISTRY.getCodec();

    TagKey<BattleParticipantEquipmentSlot> getBlockedBy();

    TagKey<BattleParticipantEquipmentSlot> getBlocks();

    RegistryEntry.Reference<BattleParticipantEquipmentSlot> getReference();

    static BattleParticipantEquipmentSlot create(final TagKey<BattleParticipantEquipmentSlot> blockedBy, final TagKey<BattleParticipantEquipmentSlot> blocks) {
        return new BattleParticipantEquipmentSlotImpl(blockedBy, blocks);
    }
}
