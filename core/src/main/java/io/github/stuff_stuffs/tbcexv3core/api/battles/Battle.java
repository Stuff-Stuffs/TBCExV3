package io.github.stuff_stuffs.tbcexv3core.api.battles;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

@ApiStatus.NonExtendable
public interface Battle extends BattleView {
    @Override
    BattleState getState();

    void trimActions(int size);

    void pushAction(BattleAction action);

    static Encoder<Battle> encoder() {
        return BattleImpl.CASTED_ENCODER;
    }

    static Decoder<Function<BattleStateMode, Battle>> decoder() {
        return BattleImpl.CASTED_DECODER;
    }
}
