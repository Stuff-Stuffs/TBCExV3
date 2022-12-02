package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@ApiStatus.NonExtendable
public interface ServerBattleWorld extends BattleWorld {
    @Nullable Battle tryGetBattle(BattleHandle handle);

    BattleHandle createBattle(Map<BattleEntity, Identifier> entities, InitialTeamSetupBattleAction teamSetupAction);
}
