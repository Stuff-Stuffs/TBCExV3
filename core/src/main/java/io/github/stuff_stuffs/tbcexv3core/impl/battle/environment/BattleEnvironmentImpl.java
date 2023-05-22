package io.github.stuff_stuffs.tbcexv3core.impl.battle.environment;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.BattleEnvironmentTraces;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironment;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEnvironmentEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.github.stuff_stuffs.tbcexv3core.internal.common.environment.BattleEnvironmentSection;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
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
        return (sectionX * ySpan + sectionY) * zSpan + sectionZ;
    }

    private void checkSetup() {
        if (state == null) {
            throw new RuntimeException("Environment not setup!");
        }
    }

    public void setup(final BattleState state) {
        this.state = state;
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState state, final Tracer<ActionTrace> tracer) {
        checkSetup();
        final int index = toIndex(pos.getX(), pos.getY(), pos.getZ());
        if (index < 0 || index >= sections.length) {
            return false;
        }
        if (this.state.getEventMap().getEvent(CoreBattleEnvironmentEvents.PRE_SET_BLOCK_STATE).getInvoker().preSetBlockState(pos, this.state, state, tracer)) {
            final BattleEnvironmentSection section = sections[index];
            final BlockState oldState = section.getBlockState(pos.getX(), pos.getY(), pos.getZ());
            section.setBlockState(pos.getX(), pos.getY(), pos.getZ(), state);
            tracer.pushInstant().value(new BattleEnvironmentTraces.EnvironmentSetBlockState(pos, oldState, state));
            this.state.getEventMap().getEvent(CoreBattleEnvironmentEvents.SUCCESSFUL_SET_BLOCK_STATE).getInvoker().successfulSetBlockState(pos, this.state, oldState, tracer);
            tracer.pop();
            return true;
        }
        this.state.getEventMap().getEvent(CoreBattleEnvironmentEvents.FAILED_SET_BLOCK_STATE).getInvoker().failedSetBlockState(pos, this.state, state, tracer);
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
        if (state.getEventMap().getEvent(CoreBattleEnvironmentEvents.PRE_SET_BATTLE_BLOCK).getInvoker().preSetBattleBlock(pos, state, Optional.ofNullable(old), tracer)) {
            if (old != null) {
                old.deinit(tracer);
            }
            tracer.pushInstant().value(new BattleEnvironmentTraces.EnvironmentSetBattleBlock(pos, old != null));
            final BattleEnvironmentBlock block = factory.create(state, pos, tracer);
            section.setBattleBlock(pos.getX(), pos.getY(), pos.getZ(), block);
            state.getEventMap().getEvent(CoreBattleEnvironmentEvents.SUCCESSFUL_SET_BATTLE_BLOCK).getInvoker().successfulSetBattleBlock(pos, state, Optional.ofNullable(old), Optional.of(block), tracer);
            tracer.pop();
            return true;
        }
        state.getEventMap().getEvent(CoreBattleEnvironmentEvents.FAILED_SET_BATTLE_BLOCK).getInvoker().failedSetBattleBlock(pos, state, Optional.ofNullable(old), tracer);
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
        if (state.getEventMap().getEvent(CoreBattleEnvironmentEvents.PRE_SET_BATTLE_BLOCK).getInvoker().preSetBattleBlock(pos, state, Optional.of(old), tracer)) {
            old.deinit(tracer);
            section.setBattleBlock(pos.getX(), pos.getY(), pos.getZ(), null);
            tracer.pushInstant().value(new BattleEnvironmentTraces.EnvironmentRemoveBattleBlock(pos));
            state.getEventMap().getEvent(CoreBattleEnvironmentEvents.SUCCESSFUL_SET_BATTLE_BLOCK).getInvoker().successfulSetBattleBlock(pos, state, Optional.of(old), Optional.empty(), tracer);
            tracer.pop();
            return true;
        }
        state.getEventMap().getEvent(CoreBattleEnvironmentEvents.FAILED_SET_BATTLE_BLOCK).getInvoker().failedSetBattleBlock(pos, state, Optional.of(old), tracer);
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

    @Override
    public BlockView asBlockView() {
        return new BlockView() {
            @Nullable
            @Override
            public BlockEntity getBlockEntity(final BlockPos pos) {
                return null;
            }

            @Override
            public BlockState getBlockState(final BlockPos pos) {
                return BattleEnvironmentImpl.this.getBlockState(pos);
            }

            @Override
            public FluidState getFluidState(final BlockPos pos) {
                return BattleEnvironmentImpl.this.getBlockState(pos).getFluidState();
            }

            @Override
            public int getHeight() {
                return max.getY() - min.getY() + 1;
            }

            @Override
            public int getBottomY() {
                return min.getY();
            }
        };
    }

    @Override
    public boolean checkForStanding(final BattleParticipantBounds bounds, final BlockPos pos, final boolean onGround) {
        final BattleParticipantBounds move = BattleParticipantBounds.move(pos, bounds);
        final Iterator<BattleParticipantBounds.Part> iterator = move.parts();
        boolean foundFloor = false;
        final BlockView blockView = asBlockView();
        while (iterator.hasNext()) {
            final BattleParticipantBounds.Part part = iterator.next();
            final int minX = MathHelper.floor(part.box().minX - 1);
            final int minY = MathHelper.floor(part.box().minY - 1);
            final int minZ = MathHelper.floor(part.box().minZ - 1);
            final int maxX = MathHelper.ceil(part.box().maxX + 1);
            final int maxY = MathHelper.ceil(part.box().maxY + 1);
            final int maxZ = MathHelper.ceil(part.box().maxZ + 1);
            final VoxelShape collisionShape = VoxelShapes.cuboid(part.box());
            final VoxelShape floorShape = VoxelShapes.cuboid(part.box().offset(0, -1, 0));
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        final BlockState state = getBlockState(new BlockPos(x, y, z));
                        final VoxelShape shape = state.getCollisionShape(blockView, pos);
                        if (shape.isEmpty()) {
                            continue;
                        }
                        final VoxelShape offset = shape.offset(x, y, z);
                        if (VoxelShapes.matchesAnywhere(collisionShape, offset, BooleanBiFunction.AND)) {
                            return false;
                        }
                        if (onGround && !foundFloor) {
                            if (VoxelShapes.matchesAnywhere(floorShape, offset, BooleanBiFunction.AND)) {
                                foundFloor = true;
                            }
                        }
                    }
                }
            }
        }
        return !onGround || foundFloor;
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
            final BattleEnvironmentSection[] sections = new BattleEnvironmentSection[this.sections.size()];
            for (int i = 0; i < sections.length; i++) {
                sections[i] = this.sections.get(i).create();
            }
            return new BattleEnvironmentImpl(outOfBoundsState, outOfBoundsBiome, min, max, sections);
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
                logger.warn("Could not set padding(Y axis) around BattleEnvironment due to world height, moved from " + minY + " to " + world.getBottomY());
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
                logger.warn("Could not set padding(Y axis) around BattleEnvironment due to world height, moved from " + maxY + " to " + (world.getTopY() - 16));
                maxY = world.getTopY() - 16;
            }
            int maxZ = bounds.maxZ + padding;
            final int highestZ = MathHelper.floor(border.getBoundSouth());
            if (maxZ > highestZ) {
                logger.warn("Could not set padding(Z axis) around BattleEnvironment due to world border, moved from " + maxZ + " to " + highestZ);
                maxZ = highestZ;
            }
            final BlockPos max = new BlockPos(maxX, maxY, maxZ);
            final int sectionMinX = minX >> 4;
            final int sectionMinY = minY >> 4;
            final int sectionMinZ = minZ >> 4;
            final int sectionMaxX = (maxX + 15) >> 4;
            final int sectionMaxY = (maxY + 15) >> 4;
            final int sectionMaxZ = (maxZ + 15) >> 4;
            final int capacity = (sectionMaxX - sectionMinX + 1) * (sectionMaxY - sectionMinY + 1) * (sectionMaxZ - sectionMinZ + 1);
            final BattleEnvironmentSection.Initial[] sections = new BattleEnvironmentSection.Initial[capacity];
            for (int x = sectionMinX; x <= sectionMaxX; x++) {
                for (int z = sectionMinZ; z <= sectionMaxZ; z++) {
                    final Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, true);
                    for (int y = sectionMinY; y <= sectionMaxY; y++) {
                        final ChunkSection section = chunk.getSection(world.sectionCoordToIndex(y));
                        final int sectionX = x - sectionMinX;
                        final int sectionY = y - sectionMinY;
                        final int sectionZ = z - sectionMinZ;
                        final int ySpan = sectionMaxY - sectionMinY + 1;
                        final int zSpan = sectionMaxZ - sectionMinZ + 1;
                        final int index = toSectionIndex(sectionX, zSpan, sectionY, ySpan, sectionZ);
                        sections[index] = BattleEnvironmentSection.of(section);
                    }
                }
            }
            return new Initial(outOfBoundsState, outOfBoundsBiome, min, max, Arrays.asList(sections));
        }

        public static int toSectionIndex(final int sectionX, final int zSpan, final int sectionY, final int ySpan, final int sectionZ) {
            return (sectionX * ySpan + sectionY) * zSpan + sectionZ;
        }
    }
}
