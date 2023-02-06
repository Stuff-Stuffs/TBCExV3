package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.registry.Registry;

public final class CoreBattleParticipantStats {
    public static final BattleParticipantStat MAX_HEALTH = BattleParticipantStat.create();

    public static void init() {
        Registry.register(BattleParticipantStat.REGISTRY, TBCExV3Core.createId("max_health"), MAX_HEALTH);
    }

    private CoreBattleParticipantStats() {
    }
}
