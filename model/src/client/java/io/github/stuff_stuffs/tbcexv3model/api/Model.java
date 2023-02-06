package io.github.stuff_stuffs.tbcexv3model.api;

import net.minecraft.util.Identifier;

import java.util.Optional;

public interface Model {
    Optional<ModelPart> byId(Identifier id);
}
