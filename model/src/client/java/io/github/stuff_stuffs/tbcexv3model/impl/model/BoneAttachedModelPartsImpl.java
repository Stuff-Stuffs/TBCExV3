package io.github.stuff_stuffs.tbcexv3model.impl.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.BoneAttachedModelParts;
import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BoneAttachedModelPartsImpl implements BoneAttachedModelParts {
    private final Map<Identifier, ModelPart> parts = new Object2ReferenceOpenHashMap<>();

    @Override
    public Set<Identifier> parts() {
        return Collections.unmodifiableSet(parts.keySet());
    }

    @Override
    public ModelPart part(final Identifier id) {
        return parts.get(id);
    }

    @Override
    public void addPart(final Identifier id, final ModelPart part) {
        parts.put(id, part);
    }

    @Override
    public void removePart(final Identifier id) {
        parts.remove(id);
    }
}
