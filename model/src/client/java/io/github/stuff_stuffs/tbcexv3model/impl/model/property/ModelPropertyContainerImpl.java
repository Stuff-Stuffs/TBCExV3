package io.github.stuff_stuffs.tbcexv3model.impl.model.property;

import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.model.properties.ModelPropertyKey;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpable;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ModelPropertyContainerImpl implements ModelPropertyContainer {
    private final Map<ModelPropertyKey<?>, Interpable<?>> map;

    public ModelPropertyContainerImpl(final Map<ModelPropertyKey<?>, Interpable<?>> map) {
        this.map = Map.copyOf(map);
    }

    @Override
    public Set<ModelPropertyKey<?>> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public <T extends Interpable<T>> Optional<T> getProperty(final ModelPropertyKey<T> key) {
        final Interpable<?> interpable = map.get(key);
        if (key.type().isInstance(interpable)) {
            return Optional.of((T) interpable);
        }
        return Optional.empty();
    }

    public static class BuilderImpl implements Builder {
        private final Map<ModelPropertyKey<?>, Interpable<?>> map = new Object2ReferenceOpenHashMap<>();

        @Override
        public <T extends Interpable<T>> Builder put(final ModelPropertyKey<T> key, final T value) {
            map.put(key, value);
            return this;
        }

        @Override
        public ModelPropertyContainer build() {
            return new ModelPropertyContainerImpl(map);
        }
    }
}
