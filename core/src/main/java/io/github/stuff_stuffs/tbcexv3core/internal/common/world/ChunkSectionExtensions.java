package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;

public interface ChunkSectionExtensions {
    void tbcex$setBlockContainer(PalettedContainer<BlockState> container);

    void tbcex$clearBlockContainer();

    void tbcex$clearBiomeContainer(Registry<Biome> registryEntry);

    void tbcex$setBiomeContainer(ReadableContainer<RegistryEntry<Biome>> container);
}
