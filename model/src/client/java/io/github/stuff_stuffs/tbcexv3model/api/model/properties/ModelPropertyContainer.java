package io.github.stuff_stuffs.tbcexv3model.api.model.properties;

import io.github.stuff_stuffs.tbcexv3model.api.util.Interpable;
import io.github.stuff_stuffs.tbcexv3model.impl.model.property.ModelPropertyContainerImpl;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@ApiStatus.NonExtendable
public interface ModelPropertyContainer {
    Set<ModelPropertyKey<?>> keys();

    <T extends Interpable<T>> Optional<T> getProperty(ModelPropertyKey<T> key);

    static ModelPropertyContainer copyFallback(final ModelPropertyContainer primary, final ModelPropertyContainer fallback) {
        final Builder builder = builder();
        final Set<ModelPropertyKey<?>> keys = primary.keys();
        for (final ModelPropertyKey<?> key : keys) {
            copyEntry(key, primary, builder);
        }
        for (final ModelPropertyKey<?> key : fallback.keys()) {
            if (!keys.contains(key)) {
                copyEntry(key, fallback, builder);
            }
        }
        return builder.build();
    }

    static ModelPropertyContainer fallback(final ModelPropertyContainer primary, final ModelPropertyContainer fallback) {
        final Set<ModelPropertyKey<?>> firstKeys = primary.keys();
        final Set<ModelPropertyKey<?>> secondKeys = fallback.keys();
        final Set<ModelPropertyKey<?>> combined = new ObjectOpenHashSet<>();
        combined.addAll(firstKeys);
        combined.addAll(secondKeys);
        return new ModelPropertyContainer() {
            @Override
            public Set<ModelPropertyKey<?>> keys() {
                return Collections.unmodifiableSet(combined);
            }

            @Override
            public <T extends Interpable<T>> Optional<T> getProperty(final ModelPropertyKey<T> key) {
                return primary.getProperty(key).or(() -> fallback.getProperty(key));
            }
        };
    }

    static ModelPropertyContainer blend(final ModelPropertyContainer first, final ModelPropertyContainer second, final double alpha) {
        final Set<ModelPropertyKey<?>> firstKeys = first.keys();
        final Set<ModelPropertyKey<?>> secondKeys = second.keys();
        final Set<ModelPropertyKey<?>> combined = new ObjectOpenHashSet<>();
        combined.addAll(firstKeys);
        combined.addAll(secondKeys);
        return new ModelPropertyContainer() {
            @Override
            public Set<ModelPropertyKey<?>> keys() {
                return Collections.unmodifiableSet(combined);
            }

            @Override
            public <T extends Interpable<T>> Optional<T> getProperty(final ModelPropertyKey<T> key) {
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

    static ModelPropertyContainer copy(final ModelPropertyContainer container) {
        final Builder builder = builder();
        for (final ModelPropertyKey<?> key : container.keys()) {
            copyEntry(key, container, builder);
        }
        return builder.build();
    }

    private static <T extends Interpable<T>> void copyEntry(final ModelPropertyKey<T> key, final ModelPropertyContainer container, final Builder builder) {
        final Optional<T> property = container.getProperty(key);
        if (property.isPresent()) {
            builder.put(key, property.get());
        }
    }

    static Builder builder() {
        return new ModelPropertyContainerImpl.BuilderImpl();
    }

    interface Builder {
        <T extends Interpable<T>> Builder put(ModelPropertyKey<T> key, T value);

        ModelPropertyContainer build();
    }
}
