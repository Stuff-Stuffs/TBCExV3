package io.github.stuff_stuffs.tbcexv3model.impl.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.Bone;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import net.minecraft.util.Identifier;

import java.util.Optional;

public class BoneImpl implements Bone {
    private final Identifier id;
    private final Optional<Identifier> parentId;
    private final Model owner;

    public BoneImpl(final Identifier id, final Optional<Identifier> parentId, final Model owner) {
        this.id = id;
        this.parentId = parentId;
        this.owner = owner;
    }

    @Override
    public Identifier id() {
        return id;
    }

    @Override
    public Optional<Identifier> parentId() {
        return parentId;
    }

    @Override
    public Model owner() {
        return owner;
    }
}
