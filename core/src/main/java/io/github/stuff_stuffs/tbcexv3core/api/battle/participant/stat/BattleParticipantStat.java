package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.stat;

import com.mojang.serialization.Lifecycle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat.BattleParticipantStatImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

public interface BattleParticipantStat {
    Registry<BattleParticipantStat> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.ofRegistry(TBCExV3Core.createId("battle_participant_stats")), Lifecycle.stable(), BattleParticipantStat::getReference)).buildAndRegister();

    RegistryEntry.Reference<BattleParticipantStat> getReference();

    static BattleParticipantStat create() {
        return new BattleParticipantStatImpl();
    }
}
