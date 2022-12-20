package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import com.mojang.serialization.Lifecycle;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat.BattleParticipantStatImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

public interface BattleParticipantStat {
    Registry<BattleParticipantStat> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleParticipantStat>ofRegistry(TBCExV3Core.createId("battle_participant_stats")), Lifecycle.stable(), false)).buildAndRegister();

    static BattleParticipantStat create() {
        return new BattleParticipantStatImpl();
    }
}
