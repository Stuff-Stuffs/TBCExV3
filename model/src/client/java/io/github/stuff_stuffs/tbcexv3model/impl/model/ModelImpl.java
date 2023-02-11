package io.github.stuff_stuffs.tbcexv3model.impl.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Skeleton;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class ModelImpl implements Model, Skeleton.Listener {
    private final Skeleton skeleton;
    private final Map<Identifier, ModelAttachedPartImpl> parts;

    public ModelImpl() {
        skeleton = Skeleton.create();
        skeleton.addListener(this);
        parts = new Object2ReferenceOpenHashMap<>();
    }

    @Override
    public Skeleton skeleton() {
        return skeleton;
    }

    @Override
    public ModelAttachedPart get(final Identifier bone) {
        return parts.get(bone);
    }

    @Override
    public void onBoneAdded(final Identifier id) {
        parts.put(id, new ModelAttachedPartImpl(id));
    }

    @Override
    public void onBoneRemoved(final Identifier id) {
        parts.remove(id);
    }

    private static final class ModelAttachedPartImpl implements ModelAttachedPart {
        private final Identifier bone;
        private final Map<Identifier, ModelPart> parts;

        private ModelAttachedPartImpl(final Identifier bone) {
            this.bone = bone;
            parts = new Object2ReferenceOpenHashMap<>();
        }

        @Override
        public Identifier bone() {
            return bone;
        }

        @Override
        public Set<Identifier> parts() {
            return Collections.unmodifiableSet(parts.keySet());
        }

        @Override
        public ModelPart part(final Identifier id) {
            return parts.get(id);
        }

        @Override
        public boolean addPart(final Identifier id, final ModelPart part) {
            if (parts.containsKey(id)) {
                return false;
            }
            parts.put(id, part);
            return true;
        }
    }
}
