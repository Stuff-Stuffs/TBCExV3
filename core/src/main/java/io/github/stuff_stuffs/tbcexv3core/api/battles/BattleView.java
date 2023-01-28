package io.github.stuff_stuffs.tbcexv3core.api.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;
import io.github.stuff_stuffs.tbcexv3core.api.util.TracerView;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleView {
    BattleStateView getState();

    int getActionCount();

    BattleAction getAction(int index);

    TracerView<ActionTrace> tracer();
}
