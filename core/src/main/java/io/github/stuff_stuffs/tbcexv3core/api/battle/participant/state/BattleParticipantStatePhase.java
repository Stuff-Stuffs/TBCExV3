package io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state;

public enum BattleParticipantStatePhase {
    SETUP(-1),
    INITIALIZATION(0),
    FIGHT(1);

    private final int order;

    BattleParticipantStatePhase(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
