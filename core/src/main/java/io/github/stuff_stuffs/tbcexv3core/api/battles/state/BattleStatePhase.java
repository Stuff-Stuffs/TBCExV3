package io.github.stuff_stuffs.tbcexv3core.api.battles.state;

public enum BattleStatePhase {
    SETUP(-1),
    INITIALIZATION(0),
    FIGHT(1),
    FINISHED(2);

    private final int order;

    BattleStatePhase(final int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
