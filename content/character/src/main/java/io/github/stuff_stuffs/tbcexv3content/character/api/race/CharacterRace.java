package io.github.stuff_stuffs.tbcexv3content.character.api.race;

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
public interface CharacterRace<D extends CharacterRacialData> {
    Identifier HUMAN = TBCExV3Character.id("human");
    Registry<CharacterRace<?>> REGISTRY = FabricRegistryBuilder.from(new SimpleDefaultedRegistry<>(HUMAN.toString(), RegistryKey.<CharacterRace<?>>ofRegistry(TBCExV3Character.id("races")), Lifecycle.stable(), false)).buildAndRegister();
    Codec<CharacterRace<?>> CODEC = REGISTRY.getCodec();

    Codec<D> codec();

    Class<D> dataClass();

    <K> DataResult<D> decode(DynamicOps<K> ops, K encoded);

    <K> DataResult<K> encode(DynamicOps<K> ops, CharacterRacialData data);

    default Optional<D> castData(final CharacterRacialData data) {
        if (dataClass().isInstance(data)) {
            return Optional.of((D) data);
        }
        return Optional.empty();
    }
}
