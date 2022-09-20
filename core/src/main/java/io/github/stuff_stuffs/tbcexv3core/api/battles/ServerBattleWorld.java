package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.InitialTeamSetupBattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntity;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public interface ServerBattleWorld extends BattleWorld {
    @Nullable Battle tryGetBattle(BattleHandle handle);

    BattleHandle createBattle(Map<BattleEntity, Identifier> entities, InitialTeamSetupBattleAction teamSetupAction);

    void pushDelayedComponent(UUID playerUuid, BattleHandle handle, BattleEntityComponent component);
}
