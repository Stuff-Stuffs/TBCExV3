package io.github.stuff_stuffs.tbcexv3content.character.api.job;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import io.github.stuff_stuffs.tbcexv3content.character.internal.common.TBCExV3Character;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleDefaultedRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.NonExtendable
public interface CharacterJob<D extends CharacterJobData> {
    Identifier UNEMPLOYED = TBCExV3Character.id("unemployed");
    Registry<CharacterJob<?>> REGISTRY = FabricRegistryBuilder.from(new SimpleDefaultedRegistry<>(UNEMPLOYED.toString(), RegistryKey.<CharacterJob<?>>ofRegistry(TBCExV3Character.id("jobs")), Lifecycle.stable(), false)).buildAndRegister();
    Codec<CharacterJob<?>> CODEC = REGISTRY.getCodec();

    Codec<D> codec();

    Class<D> dataClass();

    <K> DataResult<D> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, CharacterJobData data);

    default Optional<D> castData(final CharacterJobData data) {
        if (dataClass().isInstance(data)) {
            return Optional.of((D) data);
        }
        return Optional.empty();
    }
}
