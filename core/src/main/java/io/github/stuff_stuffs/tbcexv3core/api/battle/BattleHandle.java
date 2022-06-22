package io.github.stuff_stuffs.tbcexv3core.api.battle;

import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleHandleImpl;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

@ApiStatus.NonExtendable
public interface BattleHandle {
    RegistryKey<World> getWorldKey();

    UUID getUUID();

    static BattleHandle of(final RegistryKey<World> worldKey, final UUID uuid) {
        return new BattleHandleImpl(worldKey, uuid);
    }
}
