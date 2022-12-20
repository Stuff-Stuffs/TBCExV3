package io.github.stuff_stuffs.tbcexv3core.impl.battle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.util.CodecUtil;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.World;

import java.util.UUID;

public record BattleHandleImpl(RegistryKey<World> worldKey, UUID uuid) implements BattleHandle {
    public static final Codec<BattleHandle> CODEC = RecordCodecBuilder.create(instance -> instance.group(World.CODEC.fieldOf("world").forGetter(BattleHandle::getWorldKey), CodecUtil.UUID_CODEC.fieldOf("id").forGetter(BattleHandle::getUuid)).apply(instance, BattleHandle::of));

    @Override
    public RegistryKey<World> getWorldKey() {
        return worldKey;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }
}
