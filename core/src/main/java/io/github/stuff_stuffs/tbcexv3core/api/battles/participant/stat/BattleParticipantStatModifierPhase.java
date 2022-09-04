package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat.BattleParticipantStatModifierPhaseImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@ApiStatus.NonExtendable
public interface BattleParticipantStatModifierPhase {
    BattleParticipantStatModifierPhase ADD = BattleParticipantStatModifierPhase.create(TBCExV3Core.createId("add"), Set.of(), Set.of());
    BattleParticipantStatModifierPhase MULTIPLY = BattleParticipantStatModifierPhase.create(TBCExV3Core.createId("multiply"), Set.of(), Set.of(ADD.getId()));
    BattleParticipantStatModifierPhase POST_MULTIPLY_ADD = BattleParticipantStatModifierPhase.create(TBCExV3Core.createId("post_multiply_add"), Set.of(), Set.of(MULTIPLY.getId()));

    Identifier getId();

    Set<Identifier> getHappensBefore();

    Set<Identifier> getHappensAfter();

    static BattleParticipantStatModifierPhase create(final Identifier id, final Set<Identifier> happensBefore, final Set<Identifier> happensAfter) {
        return BattleParticipantStatModifierPhaseImpl.create(id, happensBefore, happensAfter);
    }

    static @Nullable BattleParticipantStatModifierPhase get(final Identifier id) {
        return BattleParticipantStatModifierPhaseImpl.get(id);
    }
}
