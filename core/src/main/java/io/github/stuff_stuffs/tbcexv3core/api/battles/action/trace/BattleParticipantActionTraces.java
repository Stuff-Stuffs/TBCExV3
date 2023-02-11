package io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantRemovalReason;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStat;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

public final class BattleParticipantActionTraces {
    private BattleParticipantActionTraces() {
    }

    public record BattleParticipantStartTurnActions(BattleParticipantHandle handle) implements ActionTrace {
    }

    public record BattleParticipantEndTurnActions() implements ActionTrace {

    }

    public record BattleParticipantStartMove(
            BattleParticipantHandle handle
    ) implements ActionTrace {
    }

    public record BattleParticipantMove(
            BattleParticipantHandle handle,
            BlockPos start,
            BlockPos end
    ) implements ActionTrace {
        public static final Identifier ANIMATION_DATA = TBCExV3Core.createId("participant_moved");

        @Override
        public Optional<Identifier> animationData() {
            return Optional.of(ANIMATION_DATA);
        }
    }

    public record BattleParticipantEndMove(
            BattleParticipantHandle handle
    ) implements ActionTrace {
    }

    public record BattleParticipantChangeBounds(
            BattleParticipantHandle handle,
            BattleParticipantBounds oldBounds,
            BattleParticipantBounds newBounds
    ) implements ActionTrace {
    }

    public record BattleParticipantAddEffect(
            BattleParticipantHandle handle,
            boolean combined,
            BattleParticipantEffectType<?, ?> effect
    ) implements ActionTrace {
    }

    public record BattleParticipantRemoveEffect(
            BattleParticipantHandle handle,
            BattleParticipantEffectType<?, ?> effect
    ) implements ActionTrace {
    }

    public record BattleParticipantSetTeam(
            BattleParticipantHandle handle,
            BattleParticipantTeam oldTeam,
            BattleParticipantTeam newTeam
    ) implements ActionTrace {
    }

    public record BattleParticipantJoined(
            BattleParticipantHandle handle
    ) implements ActionTrace {
        public static final Identifier ANIMATION_DATA = TBCExV3Core.createId("participant_joined");

        @Override
        public Optional<Identifier> animationData() {
            return Optional.of(ANIMATION_DATA);
        }
    }

    public record BattleParticipantLeft(
            BattleParticipantHandle handle,
            BattleParticipantRemovalReason reason
    ) implements ActionTrace {
        public static final Identifier ANIMATION_DATA = TBCExV3Core.createId("participant_left");

        @Override
        public Optional<Identifier> animationData() {
            return Optional.of(ANIMATION_DATA);
        }
    }

    public static final class Inventory {
        private Inventory() {
        }

        public record Equip(
                BattleParticipantEquipmentSlot slot,
                BattleParticipantInventoryHandle handle
        ) implements ActionTrace {
        }

        public record Unequip(
                BattleParticipantEquipmentSlot slot,
                BattleParticipantInventoryHandle handle
        ) implements ActionTrace {
        }

        public record Take(
                BattleParticipantInventoryHandle handle,
                BattleParticipantItemStack stack
        ) implements ActionTrace {
        }

        public record Give(BattleParticipantInventoryHandle handle) implements ActionTrace {
        }
    }

    public static final class Health {
        private Health() {
        }

        public record Heal(BattleParticipantHandle handle, double oldHealth, double amount) implements ActionTrace {
        }

        public record Damage(BattleParticipantHandle handle, double oldHealth, double amount) implements ActionTrace {
        }
    }

    public static final class Stat {
        private Stat() {
        }

        public record AddStatModifier(BattleParticipantStat stat) implements ActionTrace {
        }

        public record RemoveStatModifier(BattleParticipantStat stat) implements ActionTrace {
        }
    }
}
