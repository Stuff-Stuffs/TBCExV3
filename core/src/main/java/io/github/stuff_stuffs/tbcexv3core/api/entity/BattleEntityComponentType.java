package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;

public interface BattleEntityComponentType<T extends BattleEntityComponent> {
    Registry<BattleEntityComponentType<?>> REGISTRY = FabricRegistryBuilder.from(new SimpleRegistry<>(RegistryKey.<BattleEntityComponentType<?>>ofRegistry(TBCExV3Core.createId("battle_entity_components")), Lifecycle.stable(), BattleEntityComponentType::getReference)).buildAndRegister();
    Codec<BattleEntityComponentType<?>> CODEC = REGISTRY.getCodec();

    <K> DataResult<T> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, BattleEntityComponent item);

    T combine(T first, T second);

    RegistryEntry.Reference<BattleEntityComponentType<?>> getReference();

    default @Nullable T checkedCast(final BattleEntityComponent component) {
        if (component.getType() == this) {
            return (T) this;
        }
        return null;
    }
}
