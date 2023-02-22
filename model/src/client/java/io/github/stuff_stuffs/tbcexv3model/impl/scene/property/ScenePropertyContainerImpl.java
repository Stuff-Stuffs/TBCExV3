package io.github.stuff_stuffs.tbcexv3model.impl.scene.property;

import io.github.stuff_stuffs.tbcexv3model.api.scene.property.ScenePropertyContainer;
import io.github.stuff_stuffs.tbcexv3model.api.scene.property.ScenePropertyKey;
import io.github.stuff_stuffs.tbcexv3model.api.util.Interpable;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ScenePropertyContainerImpl implements ScenePropertyContainer {
    private final Map<ScenePropertyKey<?>, Interpable<?>> map;

    public ScenePropertyContainerImpl(final Map<ScenePropertyKey<?>, Interpable<?>> map) {
        this.map = Map.copyOf(map);
    }

    @Override
    public Set<ScenePropertyKey<?>> keys() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public <T extends Interpable<T>> Optional<T> getProperty(final ScenePropertyKey<T> key) {
        final Interpable<?> interpable = map.get(key);
        if (key.type().isInstance(interpable)) {
            return Optional.of((T) interpable);
        }
        return Optional.empty();
    }

    public static class BuilderImpl implements Builder {
        private final Map<ScenePropertyKey<?>, Interpable<?>> map = new Object2ReferenceOpenHashMap<>();

        @Override
        public <T extends Interpable<T>> void put(final ScenePropertyKey<T> key, final T value) {
            map.put(key, value);
        }

        @Override
        public ScenePropertyContainer build() {
            return new ScenePropertyContainerImpl(map);
        }
    }
}
