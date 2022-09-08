package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ServerBattleWorld {
    @Nullable Battle tryGetBattle(BattleHandle handle);

    BattleHandle createBattle(Set<BattleEntity> entities);
}
