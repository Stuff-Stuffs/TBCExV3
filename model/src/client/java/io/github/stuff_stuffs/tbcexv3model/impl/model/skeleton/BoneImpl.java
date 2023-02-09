package io.github.stuff_stuffs.tbcexv3model.impl.model.skeleton;

import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Bone;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Skeleton;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.Optional;

public class BoneImpl implements Bone {
    private final Identifier id;
    private Optional<Identifier> parentId;
    private final Skeleton owner;
    private Matrix4fc transform = new Matrix4f().identity();

    public BoneImpl(final Identifier id, final Optional<Identifier> parentId, final Skeleton owner) {
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
    public Skeleton owner() {
        return owner;
    }

    @Override
    public Matrix4fc transform() {
        return transform;
    }

    @Override
    public Matrix4f completeTransform() {
        if (parentId.isPresent()) {
            return owner.bone(parentId.get()).completeTransform().mul(transform);
        }
        return new Matrix4f(transform);
    }

    @Override
    public void setTransform(final Matrix4fc matrix) {
        transform = new Matrix4f(matrix);
    }

    public void setParentId(final Optional<Identifier> empty) {
        parentId = empty;
    }
}
