package io.github.stuff_stuffs.tbcexv3model.api.model.skeleton;

import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.Optional;

public interface Bone {
    Identifier id();

    Optional<Identifier> parentId();

    Skeleton owner();

    Matrix4fc transform();

    Matrix4f completeTransform();

    void setTransform(Matrix4fc matrix);
}
