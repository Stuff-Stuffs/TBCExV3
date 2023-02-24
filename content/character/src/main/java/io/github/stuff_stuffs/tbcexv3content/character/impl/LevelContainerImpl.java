package io.github.stuff_stuffs.tbcexv3content.character.impl;

import io.github.stuff_stuffs.tbcexv3content.character.api.CharacterLevelCurve;
import io.github.stuff_stuffs.tbcexv3content.character.api.LevelContainer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class LevelContainerImpl implements LevelContainer {
    private int level;
    private long totalExp;
    private long currentExp;
    private final List<IntConsumer> levelUpListeners = new ArrayList<>();

    public LevelContainerImpl() {
        level = 0;
        totalExp = 0;
        currentExp = 0;
    }

    public LevelContainerImpl(final NbtCompound compound) {
        if (compound.contains("level", NbtElement.INT_TYPE)) {
            level = compound.getInt("level");
        } else {
            level = 0;
        }
        totalExp = 0;
        for (int i = 0; i < level; i++) {
            totalExp = totalExp + CharacterLevelCurve.needed(i);
        }
        if (compound.contains("currentExp", NbtElement.LONG_TYPE)) {
            addExp(compound.getLong("currentExp"));
        }
    }

    @Override
    public int level() {
        return level;
    }

    @Override
    public long totalExp() {
        return totalExp;
    }

    @Override
    public long expTowardsNextLevel() {
        return currentExp;
    }

    @Override
    public long neededExpToLevel() {
        return CharacterLevelCurve.needed(level);
    }

    @Override
    public void addExp(final long amount) {
        final long old = totalExp;
        totalExp = totalExp + amount;
        if (totalExp < old) {
            totalExp = Long.MAX_VALUE;
        }
        currentExp = currentExp + (totalExp - old);
        while (currentExp > CharacterLevelCurve.needed(level)) {
            currentExp = currentExp - CharacterLevelCurve.needed(level);
            level++;
            for (final IntConsumer listener : levelUpListeners) {
                listener.accept(level);
            }
        }
    }

    @Override
    public long takeExp(final long amount) {
        if (currentExp < amount) {
            currentExp = 0;
            return currentExp;
        }
        currentExp = currentExp - amount;
        return amount;
    }

    @Override
    public NbtCompound toTag() {
        final NbtCompound compound = new NbtCompound();
        compound.putInt("level", level);
        compound.putLong("currentExp", currentExp);
        return compound;
    }

    @Override
    public void addLevelUpListener(final IntConsumer onLevelUp) {
        levelUpListeners.add(onLevelUp);
    }
}
