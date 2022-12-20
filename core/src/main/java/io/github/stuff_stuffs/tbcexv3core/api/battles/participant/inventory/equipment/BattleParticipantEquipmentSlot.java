package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.equipment.BattleParticipantEquipmentSlotImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantEquipmentSlot {
    Registry<BattleParticipantEquipmentSlot> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleParticipantEquipmentSlot>ofRegistry(TBCExV3Core.createId("battle_participant_equipment_slots")), Lifecycle.stable(), false)).buildAndRegister();
    Codec<BattleParticipantEquipmentSlot> CODEC = REGISTRY.getCodec();

    Text name();

    TagKey<BattleParticipantEquipmentSlot> getBlockedBy();

    TagKey<BattleParticipantEquipmentSlot> getBlocks();

    static BattleParticipantEquipmentSlot create(final Text name, final TagKey<BattleParticipantEquipmentSlot> blockedBy, final TagKey<BattleParticipantEquipmentSlot> blocks) {
        return new BattleParticipantEquipmentSlotImpl(name, blockedBy, blocks);
    }
}
