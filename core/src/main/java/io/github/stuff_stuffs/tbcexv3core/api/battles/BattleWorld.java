package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import net.fabricmc.fabric.api.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface BattleWorld {
    @Nullable BattleView tryGetBattleView(BattleHandle handle);

    List<BattleParticipantHandle> getBattles(UUID entityUuid, TriState active);
}
