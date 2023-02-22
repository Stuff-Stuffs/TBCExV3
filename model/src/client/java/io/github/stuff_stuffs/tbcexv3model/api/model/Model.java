package io.github.stuff_stuffs.tbcexv3model.api.model;

import net.minecraft.util.Identifier;

import java.util.Set;

public interface Model {
    Set<Identifier> bones();

    ModelType type();

    Bone bone(Identifier id);
}
