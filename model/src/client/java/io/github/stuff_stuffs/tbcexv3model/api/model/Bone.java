package io.github.stuff_stuffs.tbcexv3model.api.model;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.NonExtendable
public interface Bone {
    Identifier id();

    Optional<Identifier> parentId();

    Model owner();
}
