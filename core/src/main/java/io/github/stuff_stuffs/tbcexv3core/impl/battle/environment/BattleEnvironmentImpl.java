package io.github.stuff_stuffs.tbcexv3core.impl.battle.environment;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironment;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.github.stuff_stuffs.tbcexv3core.internal.common.environment.BattleEnvironmentSection;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BattleEnvironmentImpl implements BattleEnvironment {
    private final BlockState outOfBoundsState;
    private final RegistryEntry<Biome> outOfBoundsBiome;
    private final BlockPos min;
    private final BlockPos max;
    private final BattleEnvironmentSection[] sections;
    private final int sectionMinX;
    private final int sectionMinY;
    private final int sectionMinZ;
    private final int sectionMaxX;
    private final int sectionMaxY;
    private final int sectionMaxZ;
    private BattleState state = null;

    public BattleEnvironmentImpl(final BlockState outOfBoundsState, final RegistryEntry<Biome> outOfBoundsBiome, final BlockPos min, final BlockPos max, final BattleEnvironmentSection[] sections) {
        this.outOfBoundsState = outOfBoundsState;
        this.outOfBoundsBiome = outOfBoundsBiome;
        this.min = min;
        this.max = max;
        this.sections = sections;
        sectionMinX = min.getX() >> 4;
        sectionMinY = min.getY() >> 4;
        sectionMinZ = min.getZ() >> 4;
        sectionMaxX = (max.getX() + 15) >> 4;
        sectionMaxY = (max.getY() + 15) >> 4;
        sectionMaxZ = (max.getZ() + 15) >> 4;
        if (sections.length != calculateArrayLength(sectionMaxX - sectionMinX + 1, sectionMaxY - sectionMinY + 1, sectionMaxZ - sectionMinZ + 1)) {
            throw new RuntimeException("Size mismatch!");
        }
    }

    private void checkSetup() {
        if (state == null) {
            throw new RuntimeException("Environment not setup!");
        }
    }

    public void setup(final BattleState state) {
        this.state = state;
    }

    private static int calculateArrayLength(final int xSections, final int ySections, final int zSections) {
        return xSections * ySections * zSections;
    }

    private int toIndex(final int x, final int y, final int z) {
        final int sectionX = (x >> 4) - sectionMinX;
        final int sectionY = (y >> 4) - sectionMinY;
        final int sectionZ = (z >> 4) - sectionMinZ;
        final int xSpan = sectionMaxX - sectionMinX + 1;
        final int ySpan = sectionMaxY - sectionMinY + 1;
        final int zSpan = sectionMaxZ - sectionMinZ + 1;
        return (sectionX * xSpan + sectionY) * ySpan + sectionZ;
    }

    @Override
    public boolean setBiome(final BlockPos pos, final RegistryEntry<Biome> biome, final Tracer<ActionTrace> tracer) {
        checkSetup();
        final int index = toIndex(pos.getX(), pos.getY(), pos.getZ());
        if (index < 0 || index >= sections.length) {
            return false;
        }
        if (state.getEventMap().getEvent(CoreBattleEvents.PRE_BIOME_SET_EVENT).getInvoker().preBiomeSet(pos, state, biome, tracer)) {
            final BattleEnvironmentSection section = sections[index];
            final RegistryEntry<Biome> oldBiome = section.getBiome(pos.getX(), pos.getY(), pos.getZ());
            section.setBiome(pos.getX(), pos.getY(), pos.getZ(), biome);
            state.getEventMap().getEvent(CoreBattleEvents.POST_BIOME_SET_EVENT).getInvoker().postBiomeSet(pos, state, oldBiome, tracer);
            return true;
        }
        return false;
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState state, final Tracer<ActionTrace> tracer) {
        checkSetup();
        final int index = toIndex(pos.getX(), pos.getY(), pos.getZ());
        if (index < 0 || index >= sections.length) {
            return false;
        }
        if (this.state.getEventMap().getEvent(CoreBattleEvents.PRE_BLOCK_STATE_SET_EVENT).getInvoker().preBlockStateSet(pos, this.state, state, tracer)) {
            final BattleEnvironmentSection section = sections[index];
            final BlockState oldState = section.getBlockState(pos.getX(), pos.getY(), pos.getZ());
            section.setBlockState(pos.getX(), pos.getY(), pos.getZ(), state);
            this.state.getEventMap().getEvent(CoreBattleEvents.POST_BLOCK_STATE_SET_EVENT).getInvoker().postBlockStateSet(pos, this.state, oldState, tracer);
            return true;
        }
        return false;
    }

    @Override
    public boolean setBattleBlock(final BlockPos pos, final BattleEnvironmentBlock.Factory factory, final Tracer<ActionTrace> tracer) {
        checkSetup();
        final int index = toIndex(pos.getX(), pos.getY(), pos.getZ());
        if (index < 0 || index >= sections.length) {
            return false;
        }
        final BattleEnvironmentSection section = sections[index];
        final BattleEnvironmentBlock old = section.getBattleBlock(pos.getX(), pos.getY(), pos.getZ());
        if (state.getEventMap().getEvent(CoreBattleEvents.PRE_BATTLE_BLOCK_SET_EVENT).getInvoker().preBattleBlockSet(pos, state, Optional.ofNullable(old), tracer)) {
            if (old != null) {
                old.deinit(tracer);
            }
            section.setBattleBlock(pos.getX(), pos.getY(), pos.getZ(), factory.create(state, pos, tracer));
            state.getEventMap().getEvent(CoreBattleEvents.POST_BATTLE_BLOCK_SET_EVENT).getInvoker().postBattleBlockSet(pos, state, Optional.ofNullable(old), tracer);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeBattleBlock(final BlockPos pos, final Tracer<ActionTrace> tracer) {
        checkSetup();
        final int index = toIndex(pos.getX(), pos.getY(), pos.getZ());
        if (index < 0 || index >= sections.length) {
            return false;
        }
        final BattleEnvironmentSection section = sections[index];
        final BattleEnvironmentBlock old = section.getBattleBlock(pos.getX(), pos.getY(), pos.getZ());
        if (old == null) {
            return false;
        }
        if (state.getEventMap().getEvent(CoreBattleEvents.PRE_BATTLE_BLOCK_SET_EVENT).getInvoker().preBattleBlockSet(pos, state, Optional.of(old), tracer)) {
            old.deinit(tracer);
            section.setBattleBlock(pos.getX(), pos.getY(), pos.getZ(), null);
            state.getEventMap().getEvent(CoreBattleEvents.POST_BATTLE_BLOCK_SET_EVENT).getInvoker().postBattleBlockSet(pos, state, Optional.of(old), tracer);
            return true;
        }
        return false;
    }

    @Override
    public BlockPos min() {
        return min;
    }

    @Override
    public BlockPos max() {
        return max;
    }

    @Override
    public BlockState getBlockState(final BlockPos pos) {
        checkSetup();
        final int index = toIndex(pos.getX(), pos.getY(), pos.getZ());
        if (index < 0 || index >= sections.length) {
            return outOfBoundsState;
        }
        return sections[index].getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public RegistryEntry<Biome> getBiome(final BlockPos pos) {
        checkSetup();
        final int index = toIndex(pos.getX(), pos.getY(), pos.getZ());
        if (index < 0 || index >= sections.length) {
            return outOfBoundsBiome;
        }
        return sections[index].getBiome(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public Optional<BattleEnvironmentBlock> getBattleBlock(final BlockPos pos) {
        checkSetup();
        final int index = toIndex(pos.getX(), pos.getY(), pos.getZ());
        if (index < 0 || index >= sections.length) {
            return Optional.empty();
        }
        return Optional.ofNullable(sections[index].getBattleBlock(pos.getX(), pos.getY(), pos.getZ()));
    }

    public record Initial(
            BlockState outOfBoundsState,
            RegistryEntry<Biome> outOfBoundsBiome,
            BlockPos min,
            BlockPos max,
            List<BattleEnvironmentSection.Initial> sections
    ) {
        public static Codec<Initial> codec(final Registry<Biome> registry) {
            return RecordCodecBuilder.create(instance -> instance.group(
                    BlockState.CODEC.fieldOf("oob_state").forGetter(Initial::outOfBoundsState),
                    registry.createEntryCodec().fieldOf("oob_biome").forGetter(Initial::outOfBoundsBiome),
                    BlockPos.CODEC.fieldOf("min").forGetter(Initial::min),
                    BlockPos.CODEC.fieldOf("max").forGetter(Initial::max),
                    Codec.list(BattleEnvironmentSection.Initial.codec(registry)).fieldOf("sections").forGetter(Initial::sections)
            ).apply(instance, Initial::new));
        }

        public BattleEnvironment create() {
            return new BattleEnvironmentImpl(outOfBoundsState, outOfBoundsBiome, min, max, sections.stream().map(BattleEnvironmentSection.Initial::create).toArray(BattleEnvironmentSection[]::new));
        }

        public static Initial of(final BattleBounds bounds, final World world, final int padding, final BlockState outOfBoundsState, final RegistryEntry<Biome> outOfBoundsBiome) {
            Preconditions.checkArgument(padding >= 0);
            final WorldBorder border = world.getWorldBorder();
            if (!border.contains(bounds.asBox())) {
                throw new RuntimeException("Cannot build battle out of world!");
            }
            final Logger logger = TBCExV3Core.getLogger();
            int minX = bounds.minX - padding;
            final int lowestX = MathHelper.ceil(border.getBoundWest());
            if (minX < lowestX) {
                logger.warn("Could not set padding(X axis) around BattleEnvironment due to world border, moved from " + minX + " to " + lowestX);
                minX = lowestX;
            }
            int minY = bounds.minY - padding;
            if (minY < world.getBottomY()) {
                logger.warn("Could not set padding(Y axis) around BattleEnvironment due to world height, moved from " + minY + " to " + lowestX);
                minY = world.getBottomY();
            }
            int minZ = bounds.minZ - padding;
            final int lowestZ = MathHelper.ceil(border.getBoundNorth());
            if (minZ < lowestZ) {
                logger.warn("Could not set padding(Z axis) around BattleEnvironment due to world border, moved from " + minZ + " to " + lowestZ);
                minZ = lowestZ;
            }
            final BlockPos min = new BlockPos(minX, minY, minZ);

            int maxX = bounds.maxX + padding;
            final int highestX = MathHelper.floor(border.getBoundEast());
            if (maxX > highestX) {
                logger.warn("Could not set padding(X axis) around BattleEnvironment due to world border, moved from " + maxX + " to " + highestX);
                maxX = highestX;
            }
            int maxY = bounds.maxY + padding;
            if (maxY >= world.getTopY()) {
                logger.warn("Could not set padding(Y axis) around BattleEnvironment due to world height, moved from " + maxY + " to " + (world.getTopY() - 1));
                maxY = world.getTopY() - 1;
            }
            int maxZ = bounds.maxZ + padding;
            final int highestZ = MathHelper.floor(border.getBoundSouth());
            if (maxZ > highestZ) {
                logger.warn("Could not set padding(Z axis) around BattleEnvironment due to world border, moved from " + maxZ + " to " + highestZ);
                maxZ = highestZ;
            }
            final BlockPos max = new BlockPos(maxX, maxY, maxZ);
            final int sectionMinX = min.getX() >> 4;
            final int sectionMinY = min.getY() >> 4;
            final int sectionMinZ = min.getZ() >> 4;
            final int sectionMaxX = (max.getX() + 15) >> 4;
            final int sectionMaxY = (max.getY() + 15) >> 4;
            final int sectionMaxZ = (max.getZ() + 15) >> 4;
            final int capacity = calculateArrayLength(sectionMaxX - sectionMinX + 1, sectionMaxY - sectionMinY + 1, sectionMaxZ - sectionMinZ + 1);
            final BattleEnvironmentSection.Initial[] sections = new BattleEnvironmentSection.Initial[capacity];
            for (int x = sectionMinX; x <= sectionMaxX; x++) {
                for (int z = sectionMinZ; z <= sectionMaxX; z++) {
                    final Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, true);
                    for (int y = sectionMinY; y <= sectionMaxY; y++) {
                        final ChunkSection section = chunk.getSection(world.sectionCoordToIndex(y));
                        final int sectionX = (x >> 4) - sectionMinX;
                        final int sectionY = (y >> 4) - sectionMinY;
                        final int sectionZ = (z >> 4) - sectionMinZ;
                        final int xSpan = sectionMaxX - sectionMinX + 1;
                        final int ySpan = sectionMaxY - sectionMinY + 1;
                        final int index = (sectionX * xSpan + sectionY) * ySpan + sectionZ;
                        sections[index] = BattleEnvironmentSection.of(section);
                    }
                }
            }
            return new Initial(outOfBoundsState, outOfBoundsBiome, min, max, Arrays.asList(sections));
        }
    }
}
