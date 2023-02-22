package io.github.stuff_stuffs.tbcexv3model.api.model;

import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public final class ModelType {
    private static final Map<Identifier, ModelType> CACHE = new Object2ReferenceOpenHashMap<>();
    public static final ModelType PARTICIPANT = getOrCreate(TBCExV3Model.id("model_type/participant"));
    public static final ModelType ENVIRONMENT = getOrCreate(TBCExV3Model.id("model_type/environment"));
    public static final ModelType PARTICLE = getOrCreate(TBCExV3Model.id("model_type/particle"));
    private final Identifier id;
    private final @Nullable ModelType parent;

    private ModelType(final Identifier id, final @Nullable ModelType parent) {
        this.id = id;
        this.parent = parent;
    }

    public Optional<ModelType> parent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ModelType type)) {
            return false;
        }

        return id.equals(type.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ModelType{" + "id=" + id + '}';
    }

    public Identifier getId() {
        return id;
    }

    public static ModelType getOrCreate(final Identifier id) {
        return CACHE.computeIfAbsent(id, identifier -> new ModelType(identifier, null));
    }

    public static ModelType getOrCreate(final Identifier id, final ModelType parent) {
        ModelType type = CACHE.get(id);
        if (type == null) {
            type = new ModelType(id, parent);
            CACHE.put(id, type);
        }
        return type;
    }
}
