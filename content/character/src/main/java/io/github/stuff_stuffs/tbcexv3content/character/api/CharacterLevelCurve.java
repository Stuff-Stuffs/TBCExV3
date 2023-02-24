package io.github.stuff_stuffs.tbcexv3content.character.api;

public final class CharacterLevelCurve {
    private CharacterLevelCurve() {
    }

    public static long needed(final int currentLevel) {
        if (currentLevel < 0) {
            throw new IllegalArgumentException();
        }
        return 100;
    }
}
