package io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace;

public interface ActionTrace {
    final class BattleStart implements ActionTrace {
        public static final BattleStart INSTANCE = new BattleStart();

        private BattleStart() {
        }
    }

    final class BattleEnd implements ActionTrace {
        public static final BattleEnd INSTANCE = new BattleEnd();

        private BattleEnd() {
        }
    }
}
