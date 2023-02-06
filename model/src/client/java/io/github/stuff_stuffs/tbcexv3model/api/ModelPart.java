package io.github.stuff_stuffs.tbcexv3model.api;

import net.minecraft.util.Identifier;

import java.util.Optional;

public interface ModelPart {
    Identifier id();

    Optional<Identifier> parentId();
}
