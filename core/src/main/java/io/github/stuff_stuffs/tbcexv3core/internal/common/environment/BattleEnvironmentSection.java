package io.github.stuff_stuffs.tbcexv3core.internal.common.environment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.chunk.ReadableContainer;
import org.jetbrains.annotations.Nullable;

public class BattleEnvironmentSection {
    private final PalettedContainer<BlockState> blockStateContainer;
    private final PalettedContainer<RegistryEntry<Biome>> biomeContainer;
    private final Short2ObjectMap<BattleEnvironmentBlock> specialBlocks;

    public BattleEnvironmentSection(final PalettedContainer<BlockState> blockStateContainer, final PalettedContainer<RegistryEntry<Biome>> biomeContainer, final Short2ObjectMap<BattleEnvironmentBlock> specialBlocks) {
        this.blockStateContainer = blockStateContainer;
        this.biomeContainer = biomeContainer;
        this.specialBlocks = specialBlocks;
    }

    public void setBlockState(final int x, final int y, final int z, final BlockState state) {
        blockStateContainer.set(x & 15, y & 15, z & 15, state);
    }

    public void setBiome(final int x, final int y, final int z, final RegistryEntry<Biome> biome) {
        biomeContainer.set(BiomeCoords.fromBlock(x) & 3, BiomeCoords.fromBlock(y) & 3, BiomeCoords.fromBlock(z) & 3, biome);
    }

    public BlockState getBlockState(final int x, final int y, final int z) {
        return blockStateContainer.get(x & 15, y & 15, z & 15);
    }

    public RegistryEntry<Biome> getBiome(final int x, final int y, final int z) {
        return biomeContainer.get(BiomeCoords.fromBlock(x) & 3, BiomeCoords.fromBlock(y) & 3, BiomeCoords.fromBlock(z) & 3);
    }

    public static short pack(final int x, final int y, final int z) {
        final int clampedX = x & 15;
        final int clampedY = y & 15;
        final int clampedZ = z & 15;
        return (short) (clampedX | clampedY << 4 | clampedZ << 8);
    }

    public @Nullable BattleEnvironmentBlock setBattleBlock(final int x, final int y, final int z, final BattleEnvironmentBlock block) {
        if (block == null) {
            return specialBlocks.remove(pack(x, y, z));
        } else {
            return specialBlocks.put(pack(x, y, z), block);
        }
    }

    public @Nullable BattleEnvironmentBlock getBattleBlock(final int x, final int y, final int z) {
        return specialBlocks.get(pack(x, y, z));
    }

    public static Initial of(final ChunkSection section) {
        return new Initial(section.getBlockStateContainer().copy(), section.getBiomeContainer().slice());
    }

    public record Initial(
            PalettedContainer<BlockState> blockStateContainer,
            ReadableContainer<RegistryEntry<Biome>> biomeContainer
    ) {
        private static final Codec<PalettedContainer<BlockState>> BLOCK_CONTAINER_CODEC = PalettedContainer.createPalettedContainerCodec(
                Block.STATE_IDS, BlockState.CODEC, PalettedContainer.PaletteProvider.BLOCK_STATE, Blocks.AIR.getDefaultState()
        );

        public static Codec<Initial> codec(final Registry<Biome> registry) {
            return RecordCodecBuilder.create(instance -> instance.group(
                    BLOCK_CONTAINER_CODEC.fieldOf("blockstates").forGetter(Initial::blockStateContainer),
                    createBiomeCodec(registry).fieldOf("biomes").forGetter(Initial::biomeContainer)
            ).apply(instance, Initial::new));
        }

        private static Codec<ReadableContainer<RegistryEntry<Biome>>> createBiomeCodec(final Registry<Biome> biomeRegistry) {
            return PalettedContainer.createReadableContainerCodec(
                    biomeRegistry.getIndexedEntries(), biomeRegistry.createEntryCodec(), PalettedContainer.PaletteProvider.BIOME, biomeRegistry.entryOf(BiomeKeys.PLAINS)
            );
        }

        public BattleEnvironmentSection create() {
            return new BattleEnvironmentSection(blockStateContainer.copy(), biomeContainer.slice(), new Short2ObjectOpenHashMap<>());
        }
    }
}
