package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStat;
import net.minecraft.registry.entry.RegistryEntry;

public class BattleParticipantStatImpl implements BattleParticipantStat {
    private final RegistryEntry.Reference<BattleParticipantStat> reference;

    public BattleParticipantStatImpl() {
        reference = BattleParticipantStat.REGISTRY.createEntry(this);
    }

    @Override
    public RegistryEntry.Reference<BattleParticipantStat> getReference() {
        return reference;
    }
}
