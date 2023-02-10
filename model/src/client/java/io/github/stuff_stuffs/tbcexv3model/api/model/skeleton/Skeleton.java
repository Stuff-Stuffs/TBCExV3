package io.github.stuff_stuffs.tbcexv3model.api.model.skeleton;

import io.github.stuff_stuffs.tbcexv3model.impl.model.skeleton.SkeletonImpl;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.Set;

public interface Skeleton {
    Set<Identifier> bones();

    Bone bone(Identifier id);

    boolean addBone(Identifier id, Optional<Identifier> parentId);

    boolean removeBone(Identifier id);

    void addListener(Listener listener);

    static Skeleton create() {
        return new SkeletonImpl();
    }

    interface Listener {
        void onBoneAdded(Identifier id);

        void onBoneRemoved(Identifier id);
    }
}
