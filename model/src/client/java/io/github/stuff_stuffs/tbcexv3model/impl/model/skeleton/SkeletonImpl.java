package io.github.stuff_stuffs.tbcexv3model.impl.model.skeleton;

import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Bone;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Skeleton;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.*;

public class SkeletonImpl implements Skeleton {
    private final Map<Identifier, BoneImpl> bones = new Object2ReferenceOpenHashMap<>();
    private final List<Listener> listeners = new ArrayList<>();

    @Override
    public Set<Identifier> bones() {
        return Collections.unmodifiableSet(bones.keySet());
    }

    @Override
    public Bone bone(final Identifier id) {
        return bones.get(id);
    }

    @Override
    public boolean addBone(final Identifier id, final Optional<Identifier> parentId) {
        if (bones.containsKey(id)) {
            return false;
        } else {
            if (parentId.isPresent() && !bones.containsKey(parentId.get())) {
                return false;
            }
            bones.put(id, new BoneImpl(id, parentId, this));
            for (final Listener listener : listeners) {
                listener.onBoneAdded(id);
            }
            return true;
        }
    }

    @Override
    public boolean removeBone(final Identifier id) {
        if (!bones.containsKey(id)) {
            return false;
        }
        for (final BoneImpl value : bones.values()) {
            if (value.parentId().isPresent() && value.parentId().get().equals(id)) {
                value.setParentId(Optional.empty());
            }
        }
        bones.remove(id);
        for (final Listener listener : listeners) {
            listener.onBoneRemoved(id);
        }
        return true;
    }

    @Override
    public void addListener(final Listener listener) {
        listeners.add(listener);
    }
}
