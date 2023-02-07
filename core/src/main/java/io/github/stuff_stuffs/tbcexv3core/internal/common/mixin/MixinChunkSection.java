package io.github.stuff_stuffs.tbcexv3core.internal.common.mixin;

import io.github.stuff_stuffs.tbcexv3core.internal.common.world.ChunkSectionExtensions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkSection.class)
public abstract class MixinChunkSection implements ChunkSectionExtensions {
    @Mutable
    @Shadow
    @Final
    private PalettedContainer<BlockState> blockStateContainer;

    @Shadow
    private ReadableContainer<RegistryEntry<Biome>> biomeContainer;

    @Shadow public abstract void calculateCounts();

    @Override
    public void tbcex$setBlockContainer(final PalettedContainer<BlockState> container) {
        blockStateContainer = container;
        calculateCounts();
    }

    @Override
    public void tbcex$clearBlockContainer() {
        blockStateContainer = new PalettedContainer<>(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
        calculateCounts();
    }

    @Override
    public void tbcex$clearBiomeContainer(final Registry<Biome> biomeRegistry) {
        biomeContainer = new PalettedContainer<>(biomeRegistry.getIndexedEntries(), biomeRegistry.entryOf(BiomeKeys.PLAINS), PalettedContainer.PaletteProvider.BIOME);
    }

    @Override
    public void tbcex$setBiomeContainer(final ReadableContainer<RegistryEntry<Biome>> container) {
        biomeContainer = container;
    }
}
