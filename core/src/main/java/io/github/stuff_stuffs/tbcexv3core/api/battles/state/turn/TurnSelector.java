package io.github.stuff_stuffs.tbcexv3core.api.battles.state.turn;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateView;

public interface TurnSelector {
    boolean isCurrentTurn(BattleStateView state, BattleParticipantHandle handle);

    void init(BattleStateView state);

    void deinit();

    static TurnSelector any() {
        return new TurnSelector() {
            @Override
            public boolean isCurrentTurn(final BattleStateView state, final BattleParticipantHandle handle) {
                return true;
            }

            @Override
            public void init(final BattleStateView state) {

            }

            @Override
            public void deinit() {

            }
        };
    }
}
