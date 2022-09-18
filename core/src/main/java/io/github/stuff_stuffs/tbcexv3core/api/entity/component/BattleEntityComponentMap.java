package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import io.github.stuff_stuffs.tbcexv3core.impl.entity.BattleEntityComponentMapImpl;

import java.util.Optional;

public interface BattleEntityComponentMap {
    <T extends BattleEntityComponent> Optional<T> get(BattleEntityComponentType<T> type);

    interface Builder {
        <T extends BattleEntityComponent> Builder add(BattleEntityComponentType<T> type, T value);

        BattleEntityComponentMap build();
    }

    static Builder builder() {
        return BattleEntityComponentMapImpl.builder();
    }
}
