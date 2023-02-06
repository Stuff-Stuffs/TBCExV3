package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@ApiStatus.NonExtendable
public interface ServerBattleWorld extends BattleWorld {
    @Nullable Battle tryGetBattle(BattleHandle handle);

    BattleHandle createBattle(Map<BattleEntity, Identifier> entities, InitialTeamSetupBattleAction teamSetupAction);

    BattleHandle createBattle(Map<BattleEntity, Identifier> entities, InitialTeamSetupBattleAction teamSetupAction, BattleBounds bounds, int padding);

    void pushDelayedPlayerComponent(UUID uuid, BattleHandle handle, BattleEntityComponent component);
}
