package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import net.minecraft.util.Identifier;

public final class RequiredAnimations {
    public static final class Participant {
        public static Identifier IDLE = TBCExV3Model.id("animation/idle");
        public static Identifier DAMAGE_TAKEN = TBCExV3Model.id("animation/damage_taken");
        public static Identifier DEATH = TBCExV3Model.id("animation/death");
        public static Identifier SPAWN = TBCExV3Model.id("animation/spawn");

        private Participant() {
        }
    }

    public static final class Environment {
        public static final Identifier PHASE_IN = TBCExV3Model.id("animation/phase_in");
        public static final Identifier PHASE_OUT = TBCExV3Model.id("animation/phase_out");

        private Environment() {
        }
    }

    public static final class Particle {
        public static final Identifier PHASE_IN = TBCExV3Model.id("animation/phase_in");
        public static final Identifier PHASE_OUT = TBCExV3Model.id("animation/phase_out");

        private Particle() {
        }
    }

    private RequiredAnimations() {
    }
}
