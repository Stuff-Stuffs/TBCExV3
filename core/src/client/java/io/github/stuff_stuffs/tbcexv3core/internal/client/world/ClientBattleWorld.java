package io.github.stuff_stuffs.tbcexv3core.internal.client.world;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ClientBattleWorld {
    void update(BattleUpdate update);

    void update(UUID entityId, List<BattleParticipantHandle> battleIds, List<BattleParticipantHandle> inactiveBattles);

    void tbcex$render(WorldRenderContext context);

    void tbcex$close();

    @Nullable AnimationScene tbcex$getScene(BattleHandle handle);
}
