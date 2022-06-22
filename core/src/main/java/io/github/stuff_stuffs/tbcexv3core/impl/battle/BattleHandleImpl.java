package io.github.stuff_stuffs.tbcexv3core.impl.battle;

import io.github.stuff_stuffs.tbcexv3core.api.battle.BattleHandle;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.UUID;

public record BattleHandleImpl(RegistryKey<World> worldKey, UUID uuid) implements BattleHandle {
    @Override
    public RegistryKey<World> getWorldKey() {
        return worldKey;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
