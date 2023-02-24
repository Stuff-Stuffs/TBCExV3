package io.github.stuff_stuffs.tbcexv3content.character.api;

import io.github.stuff_stuffs.tbcexv3content.character.impl.CharacterStatContainerImpl;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.stat.BattleParticipantStat;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface CharacterStatContainer {
    int getLevel(BattleParticipantStat stat);

    void setLevel(BattleParticipantStat stat, int level);

    NbtCompound toTag();

    static CharacterStatContainer create() {
        return new CharacterStatContainerImpl();
    }

    static CharacterStatContainer fromTag(final NbtCompound compound) {
        return new CharacterStatContainerImpl(compound);
    }
}
