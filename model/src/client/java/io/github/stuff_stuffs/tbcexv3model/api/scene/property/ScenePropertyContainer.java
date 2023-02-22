package io.github.stuff_stuffs.tbcexv3model.api.scene.property;

import io.github.stuff_stuffs.tbcexv3model.api.util.Interpable;
import io.github.stuff_stuffs.tbcexv3model.impl.scene.property.ScenePropertyContainerImpl;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@ApiStatus.NonExtendable
public interface ScenePropertyContainer {
    Set<ScenePropertyKey<?>> keys();

    <T extends Interpable<T>> Optional<T> getProperty(ScenePropertyKey<T> key);

    static ScenePropertyContainer copyFallback(final ScenePropertyContainer primary, final ScenePropertyContainer fallback) {
        final Builder builder = builder();
        final Set<ScenePropertyKey<?>> keys = primary.keys();
        for (final ScenePropertyKey<?> key : keys) {
            copyEntry(key, primary, builder);
        }
        for (final ScenePropertyKey<?> key : fallback.keys()) {
            if (!keys.contains(key)) {
                copyEntry(key, fallback, builder);
            }
        }
        return builder.build();
    }

    static ScenePropertyContainer fallback(final ScenePropertyContainer primary, final ScenePropertyContainer fallback) {
        final Set<ScenePropertyKey<?>> firstKeys = primary.keys();
        final Set<ScenePropertyKey<?>> secondKeys = fallback.keys();
        final Set<ScenePropertyKey<?>> combined = new ObjectOpenHashSet<>();
        combined.addAll(firstKeys);
        combined.addAll(secondKeys);
        return new ScenePropertyContainer() {
            @Override
            public Set<ScenePropertyKey<?>> keys() {
                return Collections.unmodifiableSet(combined);
            }

            @Override
            public <T extends Interpable<T>> Optional<T> getProperty(final ScenePropertyKey<T> key) {
                return primary.getProperty(key).or(() -> fallback.getProperty(key));
            }
        };
    }

    static ScenePropertyContainer copy(final ScenePropertyContainer container) {
        final Builder builder = builder();
        for (final ScenePropertyKey<?> key : container.keys()) {
            copyEntry(key, container, builder);
        }
        return builder.build();
    }

    static ScenePropertyContainer blend(final ScenePropertyContainer first, final ScenePropertyContainer second, final double alpha) {
        return new ScenePropertyContainer() {
            @Override
            public Set<ScenePropertyKey<?>> keys() {
                final Set<ScenePropertyKey<?>> firstKeys = first.keys();
                final Set<ScenePropertyKey<?>> secondKeys = second.keys();
                final Set<ScenePropertyKey<?>> combined = new ObjectOpenHashSet<>();
                combined.addAll(firstKeys);
                combined.addAll(secondKeys);
                return combined;
            }

            @Override
            public <T extends Interpable<T>> Optional<T> getProperty(final ScenePropertyKey<T> key) {
                final Optional<T> firstProperty = first.getProperty(key);
                final Optional<T> secondProperty = second.getProperty(key);
                if (firstProperty.isPresent() && secondProperty.isPresent()) {
                    return Optional.of(firstProperty.get().interpolate(secondProperty.get(), alpha));
                } else {
                    return firstProperty.isPresent() ? firstProperty : secondProperty;
                }
            }
        };
    }

    private static <T extends Interpable<T>> void copyEntry(final ScenePropertyKey<T> key, final ScenePropertyContainer container, final Builder builder) {
        final Optional<T> property = container.getProperty(key);
        if (property.isPresent()) {
            builder.put(key, property.get());
        }
    }

    static ScenePropertyContainer.Builder builder() {
        return new ScenePropertyContainerImpl.BuilderImpl();
    }

    interface Builder {
        <T extends Interpable<T>> void put(ScenePropertyKey<T> key, T value);

        ScenePropertyContainer build();
    }
}
