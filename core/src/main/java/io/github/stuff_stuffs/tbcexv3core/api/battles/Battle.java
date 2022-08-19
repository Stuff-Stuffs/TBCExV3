package io.github.stuff_stuffs.tbcexv3core.api.battles;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface Battle extends BattleView {
    @Override
    BattleState getState();

    void trimActions(int size);

    void pushAction(BattleAction action);

    static Codec<Battle> codec() {
        return BattleImpl.CASTED_CODEC;
    }
}
