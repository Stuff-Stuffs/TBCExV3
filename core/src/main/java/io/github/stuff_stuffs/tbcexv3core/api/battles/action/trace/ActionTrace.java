package io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace;

import net.minecraft.util.Identifier;

import java.util.Optional;

public interface ActionTrace {
    default Optional<Identifier> animationData() {
        return Optional.empty();
    }

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
