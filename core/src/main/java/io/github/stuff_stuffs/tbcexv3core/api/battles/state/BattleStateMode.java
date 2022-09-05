package io.github.stuff_stuffs.tbcexv3core.api.battles.state;

public enum BattleStateMode {
    SERVER(true),
    CLIENT(false);
    private final boolean verifyControlToken;

    BattleStateMode(final boolean verifyControlToken) {
        this.verifyControlToken = verifyControlToken;
    }

    public boolean shouldVerifyControlToken() {
        return verifyControlToken;
    }
}
