package io.github.stuff_stuffs.tbcexv3content.character.impl;

import io.github.stuff_stuffs.tbcexv3content.character.api.CharacterStatContainer;
import io.github.stuff_stuffs.tbcexv3content.character.internal.common.TBCExV3Character;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStat;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

public class CharacterStatContainerImpl implements CharacterStatContainer {
    private final Object2IntMap<BattleParticipantStat> stats = new Object2IntOpenHashMap<>();

    public CharacterStatContainerImpl() {
    }

    public CharacterStatContainerImpl(final NbtCompound compound) {
        for (final String key : compound.getKeys()) {
            if (!compound.contains(key, NbtElement.INT_TYPE)) {
                TBCExV3Character.LOGGER.error("Non int stat entry!");
                continue;
            }
            final Identifier id = Identifier.tryParse(key);
            if (id == null) {
                TBCExV3Character.LOGGER.error("Illegal stat name {}!", key);
            } else {
                final BattleParticipantStat stat = BattleParticipantStat.REGISTRY.get(id);
                if (stat == null) {
                    TBCExV3Character.LOGGER.error("Unrecognized stat {}!", key);
                } else {
                    stats.put(stat, compound.getInt(key));
                }
            }
        }
    }

    @Override
    public int getLevel(final BattleParticipantStat stat) {
        return stats.getOrDefault(stat, 0);
    }

    @Override
    public void setLevel(final BattleParticipantStat stat, final int level) {
        stats.put(stat, level);
    }

    @Override
    public NbtCompound toTag() {
        final NbtCompound compound = new NbtCompound();
        for (final Object2IntMap.Entry<BattleParticipantStat> entry : stats.object2IntEntrySet()) {
            final Identifier id = BattleParticipantStat.REGISTRY.getId(entry.getKey());
            if (id == null) {
                TBCExV3Character.LOGGER.error("BattleParticipantStat not registered, not saving!");
            } else {
                compound.putInt(id.toString(), entry.getIntValue());
            }
        }
        return compound;
    }
}
