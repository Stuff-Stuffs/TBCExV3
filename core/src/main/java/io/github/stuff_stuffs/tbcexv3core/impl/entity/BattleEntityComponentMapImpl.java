package io.github.stuff_stuffs.tbcexv3core.impl.entity;

import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentMap;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.Map;
import java.util.Optional;

public class BattleEntityComponentMapImpl implements BattleEntityComponentMap {
    private final Map<BattleEntityComponentType<?>, BattleEntityComponent> components;

    public BattleEntityComponentMapImpl(final Map<BattleEntityComponentType<?>, BattleEntityComponent> components) {
        this.components = Map.copyOf(components);
    }

    @Override
    public <T extends BattleEntityComponent> Optional<T> get(final BattleEntityComponentType<T> type) {
        return Optional.ofNullable((T) components.getOrDefault(type, null));
    }

    private static final class BuilderImpl implements Builder {
        private final Map<BattleEntityComponentType<?>, BattleEntityComponent> components = new Reference2ObjectOpenHashMap<>();

        @Override
        public <T extends BattleEntityComponent> Builder add(final BattleEntityComponentType<T> type, final T value) {
            if (components.put(type, value) != null) {
                throw new RuntimeException("Duplicate entity components!");
            }
            return this;
        }

        @Override
        public BattleEntityComponentMap build() {
            return new BattleEntityComponentMapImpl(components);
        }
    }

    public static Builder builder() {
        return new BuilderImpl();
    }
}
