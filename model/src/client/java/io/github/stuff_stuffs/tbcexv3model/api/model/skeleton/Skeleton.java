package io.github.stuff_stuffs.tbcexv3model.api.model.skeleton;

import io.github.stuff_stuffs.tbcexv3model.impl.model.skeleton.SkeletonImpl;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface Skeleton extends SkeletonView {
    @Override
    Bone bone(Identifier id);

    boolean addBone(Identifier id, Optional<Identifier> parentId);

    boolean removeBone(Identifier id);

    static Skeleton create() {
        return new SkeletonImpl();
    }
}
