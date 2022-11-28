package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory.equipment;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.registry.RegistryEntry;

public class BattleParticipantEquipmentSlotImpl implements BattleParticipantEquipmentSlot {
    private final Text name;
    private final TagKey<BattleParticipantEquipmentSlot> blockedBy;
    private final TagKey<BattleParticipantEquipmentSlot> blocks;
    private final RegistryEntry.Reference<BattleParticipantEquipmentSlot> reference;

    public BattleParticipantEquipmentSlotImpl(final Text name, final TagKey<BattleParticipantEquipmentSlot> blockedBy, final TagKey<BattleParticipantEquipmentSlot> blocks) {
        this.name = name;
        this.blockedBy = blockedBy;
        this.blocks = blocks;
        reference = BattleParticipantEquipmentSlot.REGISTRY.createEntry(this);
    }

    @Override
    public Text name() {
        return name;
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
