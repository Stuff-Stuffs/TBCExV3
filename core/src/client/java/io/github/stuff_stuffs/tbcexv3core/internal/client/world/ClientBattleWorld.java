package io.github.stuff_stuffs.tbcexv3core.internal.client.world;

import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;

import java.util.List;
import java.util.UUID;

public interface ClientBattleWorld {
    void update(BattleUpdate update);

    void update(UUID entityId, List<UUID> battleIds, List<UUID> inactiveBattles);
}
