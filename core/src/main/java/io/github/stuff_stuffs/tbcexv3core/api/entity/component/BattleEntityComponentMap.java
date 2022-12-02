package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import io.github.stuff_stuffs.tbcexv3core.impl.entity.BattleEntityComponentMapImpl;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.NonExtendable
public interface BattleEntityComponentMap {
    <T extends BattleEntityComponent> Optional<T> get(BattleEntityComponentType<T> type);

    Iterable<? extends BattleEntityComponent> components();

    interface Builder {
        <T extends BattleEntityComponent> Builder add(BattleEntityComponentType<T> type, T value);

        BattleEntityComponentMap build();
    }

    static Builder builder() {
        return BattleEntityComponentMapImpl.builder();
    }
}
