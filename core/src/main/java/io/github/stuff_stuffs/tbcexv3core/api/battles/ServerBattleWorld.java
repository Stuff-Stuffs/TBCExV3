package io.github.stuff_stuffs.tbcexv3core.api.battles;

import org.jetbrains.annotations.Nullable;

public interface ServerBattleWorld {
    @Nullable Battle tryGetBattle(BattleHandle handle);
}
