package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state;

public enum BattleParticipantStatePhase {
    SETUP(-1),
    INITIALIZATION(0),
    FIGHT(1),
    FINISHED(2);

    private final int order;

    BattleParticipantStatePhase(final int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
