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
import net.minecraft.util.math.BlockPos;

public final class ParticipantActionTraces {
    private ParticipantActionTraces() {
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
    }

    public record BattleParticipantLeft(
            BattleParticipantHandle handle,
            BattleParticipantRemovalReason reason
    ) implements ActionTrace {
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
