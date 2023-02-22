package io.github.stuff_stuffs.tbcexv3model.impl.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.Bone;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModelImpl implements Model {
    private final Map<Identifier, Bone> bones;
    private final ModelType type;

    public ModelImpl(final Map<Identifier, Optional<Identifier>> parentMap, final ModelType type) {
        this.type = type;
        bones = new Object2ReferenceOpenHashMap<>();
        for (final Map.Entry<Identifier, Optional<Identifier>> entry : parentMap.entrySet()) {
            bones.put(entry.getKey(), new BoneImpl(entry.getKey(), entry.getValue(), this));
        }
    }

    @Override
    public Set<Identifier> bones() {
        return Collections.unmodifiableSet(bones.keySet());
    }

    @Override
    public ModelType type() {
        return type;
    }

    @Override
    public Bone bone(final Identifier id) {
        return bones.get(id);
    }
}
