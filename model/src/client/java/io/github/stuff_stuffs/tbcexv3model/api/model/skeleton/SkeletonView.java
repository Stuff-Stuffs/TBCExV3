package io.github.stuff_stuffs.tbcexv3model.api.model.skeleton;

import net.minecraft.util.Identifier;

import java.util.Set;

public interface SkeletonView {
    Set<Identifier> bones();

    BoneView bone(Identifier id);

    void addListener(Listener listener);

    interface Listener {
        void onBoneAdded(Identifier id);

        void onBoneRemoved(Identifier id);
    }
}
