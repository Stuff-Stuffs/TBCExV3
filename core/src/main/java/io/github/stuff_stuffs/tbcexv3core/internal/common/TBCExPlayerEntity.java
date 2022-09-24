package io.github.stuff_stuffs.tbcexv3core.internal.common;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import org.jetbrains.annotations.Nullable;

public interface TBCExPlayerEntity {
    void tbcex$setCurrentBattle(@Nullable BattleHandle handle);

    @Nullable BattleHandle tbcex$getCurrentBattle();
}
