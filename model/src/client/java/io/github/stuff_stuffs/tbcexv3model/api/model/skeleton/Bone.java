package io.github.stuff_stuffs.tbcexv3model.api.model.skeleton;

import org.joml.Matrix4fc;

public interface Bone extends BoneView {
    void setTransform(Matrix4fc matrix);
}
