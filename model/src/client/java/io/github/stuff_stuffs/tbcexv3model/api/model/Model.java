package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.api.model.modelpart.ModelPart;
import io.github.stuff_stuffs.tbcexv3model.api.model.skeleton.Skeleton;
import io.github.stuff_stuffs.tbcexv3model.impl.model.ModelImpl;
import net.minecraft.util.Identifier;

public interface Model extends ModelView {
    @Override
    Skeleton skeleton();

    @Override
    ModelAttachedPart get(Identifier bone);

    static Model create(final ModelType type) {
        return new ModelImpl(type);
    }

    interface ModelAttachedPart extends ModelAttachedPartView {
        boolean addPart(Identifier id, ModelPart part);

        boolean removePart(Identifier id);
    }
}
