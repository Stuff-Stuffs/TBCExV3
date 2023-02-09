package io.github.stuff_stuffs.tbcexv3model.api.animation;

import net.minecraft.util.Identifier;

public sealed interface AnimationResource {
    record ModelResource(Identifier id) implements AnimationResource {
    }

    record BoneResource(ModelResource resource, Identifier id) implements AnimationResource {
    }
}
