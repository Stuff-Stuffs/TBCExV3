package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.stat;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStatModifierPhase;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class BattleParticipantStatModifierPhaseImpl implements BattleParticipantStatModifierPhase {
    private static final Map<Identifier, BattleParticipantStatModifierPhase> PHASES = new Object2ReferenceOpenHashMap<>();
    private final Identifier id;
    private final Set<Identifier> happensBefore;
    private final Set<Identifier> happensAfter;

    public BattleParticipantStatModifierPhaseImpl(final Identifier id, final Set<Identifier> happensBefore, final Set<Identifier> happensAfter) {
        this.id = id;
        this.happensBefore = happensBefore;
        this.happensAfter = happensAfter;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Set<Identifier> getHappensBefore() {
        return happensBefore;
    }

    @Override
    public Set<Identifier> getHappensAfter() {
        return happensAfter;
    }

    public static BattleParticipantStatModifierPhase create(final Identifier id, final Set<Identifier> happensBefore, final Set<Identifier> happensAfter) {
        final BattleParticipantStatModifierPhase phase = new BattleParticipantStatModifierPhaseImpl(id, Set.copyOf(happensBefore), Set.copyOf(happensAfter));
        if (PHASES.put(id, phase) != null) {
            throw new RuntimeException("Duplicate stat modifier phases!");
        }
        return phase;
    }

    public static @Nullable BattleParticipantStatModifierPhase get(final Identifier id) {
        return PHASES.get(id);
    }
}
