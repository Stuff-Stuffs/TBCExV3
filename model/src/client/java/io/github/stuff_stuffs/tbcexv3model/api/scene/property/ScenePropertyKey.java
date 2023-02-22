package io.github.stuff_stuffs.tbcexv3model.api.scene.property;

import io.github.stuff_stuffs.tbcexv3model.api.util.Interpable;
import net.minecraft.util.Identifier;

public record ScenePropertyKey<T extends Interpable<T>>(Identifier id, Class<T> type) {
}
