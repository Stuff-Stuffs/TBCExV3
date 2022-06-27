package io.github.stuff_stuffs.tbcexv3core.impl.battle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battle.BattleHandle;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.UUID;

public record BattleHandleImpl(RegistryKey<World> worldKey, UUID uuid) implements BattleHandle {
    public static final Codec<BattleHandle> CODEC = RecordCodecBuilder.create(instance -> instance.group(World.CODEC.fieldOf("world").forGetter(BattleHandle::getWorldKey), Codecs.UUID.fieldOf("id").forGetter(BattleHandle::getUuid)).apply(instance, BattleHandle::of));

    @Override
    public RegistryKey<World> getWorldKey() {
        return worldKey;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }
}
