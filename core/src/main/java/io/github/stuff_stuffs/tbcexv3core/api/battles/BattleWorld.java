package io.github.stuff_stuffs.tbcexv3core.api.battles;

import org.jetbrains.annotations.Nullable;

public interface BattleWorld {
    @Nullable BattleAccess tryGetBattleAccess(BattleHandle handle);
}
