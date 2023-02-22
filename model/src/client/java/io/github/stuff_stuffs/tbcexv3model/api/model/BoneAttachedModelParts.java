package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Set;

@ApiStatus.NonExtendable
public interface BoneAttachedModelParts {
    Set<Identifier> parts();

    ModelPart part(Identifier id);

    void addPart(Identifier id, ModelPart part);

    void removePart(Identifier id);
}
