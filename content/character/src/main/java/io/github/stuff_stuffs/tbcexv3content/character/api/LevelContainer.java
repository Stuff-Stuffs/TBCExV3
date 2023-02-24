package io.github.stuff_stuffs.tbcexv3content.character.api;

import io.github.stuff_stuffs.tbcexv3content.character.impl.LevelContainerImpl;
import net.minecraft.nbt.NbtCompound;

import java.util.function.IntConsumer;

public interface LevelContainer {
    int level();

    long totalExp();

    long expTowardsNextLevel();

    long neededExpToLevel();

    void addExp(long amount);

    long takeExp(long amount);

    NbtCompound toTag();

    void addLevelUpListener(IntConsumer onLevelUp);

    static LevelContainer create() {
        return new LevelContainerImpl();
    }

    static LevelContainer fromTag(final NbtCompound compound) {
        return new LevelContainerImpl(compound);
    }
}
