package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.equipment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.RegistryEntry;

public class BattleParticipantEquipmentSlotImpl implements BattleParticipantEquipmentSlot {
    private final TagKey<BattleParticipantEquipmentSlot> blockedBy;
    private final TagKey<BattleParticipantEquipmentSlot> blocks;
    private final RegistryEntry.Reference<BattleParticipantEquipmentSlot> reference;

    public BattleParticipantEquipmentSlotImpl(final TagKey<BattleParticipantEquipmentSlot> blockedBy, final TagKey<BattleParticipantEquipmentSlot> blocks) {
        this.blockedBy = blockedBy;
        this.blocks = blocks;
        reference = BattleParticipantEquipmentSlot.REGISTRY.createEntry(this);
    }

    @Override
    public TagKey<BattleParticipantEquipmentSlot> getBlockedBy() {
        return blockedBy;
    }

    @Override
    public TagKey<BattleParticipantEquipmentSlot> getBlocks() {
        return blocks;
    }

    @Override
    public RegistryEntry.Reference<BattleParticipantEquipmentSlot> getReference() {
        return reference;
    }
}
