package io.github.stuff_stuffs.tbcexv3core.impl.battle.environment;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironment;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import io.github.stuff_stuffs.tbcexv3core.internal.common.environment.BattleEnvironmentSection;
import io.github.stuff_stuffs.tbcexv3util.api.util.Tracer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.block.BlockState;
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
import org.slf4j.Logger;

import java.util.*;

public class BattleEnvironmentImpl implements BattleEnvironment {
    private final BlockState outOfBoundsState;
    private final RegistryEntry<Biome> outOfBoundsBiome;
    private final BlockPos min;
    private final BlockPos max;
    private final World delegate;
    private final Map<BlockPos, BattleEnvironmentBlock> specialBlocks;
    private BlockPos realOrigin;
    private BattleState state = null;

    public BattleEnvironmentImpl(final BlockState outOfBoundsState, final RegistryEntry<Biome> outOfBoundsBiome, final BlockPos min, final BlockPos max, final World delegate) {
        this.outOfBoundsState = outOfBoundsState;
        this.outOfBoundsBiome = outOfBoundsBiome;
        this.min = min;
        this.max = max;
        this.delegate = delegate;
        specialBlocks = new Object2ReferenceOpenHashMap<>();
    }

    private void checkSetup() {
        if (state == null) {
            throw new RuntimeException("Environment not setup!");
        }
    }

    public void setup(final BattleState state, final BlockPos origin) {
        this.state = state;
        realOrigin = origin;
    }

    @Override
    public BlockPos origin() {
        return realOrigin;
    }

    @Override
    public boolean setBlockState(final BlockPos pos, final BlockState state, final Tracer<ActionTrace> tracer) {
        checkSetup();
        if (!(min.getX() <= pos.getX() && min.getY() <= pos.getY() && min.getZ() <= pos.getZ() &&
                pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ())) {
            return false;
        }
        if (this.state.getEventMap().getEvent(CoreBattleEvents.PRE_BLOCK_STATE_SET_EVENT).getInvoker().preBlockStateSet(pos, this.state, state, tracer)) {
            final BlockPos realPos = pos.subtract(min).add(realOrigin);
            final BlockState oldState = delegate.getBlockState(realPos);
            delegate.setBlockState(realPos, state);
            this.state.getEventMap().getEvent(CoreBattleEvents.POST_BLOCK_STATE_SET_EVENT).getInvoker().postBlockStateSet(pos, this.state, oldState, tracer);
            return true;
        }
        return false;
    }

    @Override
    public boolean setBattleBlock(final BlockPos pos, final BattleEnvironmentBlock.Factory factory, final Tracer<ActionTrace> tracer) {
        checkSetup();
        if (!(min.getX() <= pos.getX() && min.getY() <= pos.getY() && min.getZ() <= pos.getZ() &&
                pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ())) {
            return false;
        }
        final BattleEnvironmentBlock old = specialBlocks.get(pos);
        if (state.getEventMap().getEvent(CoreBattleEvents.PRE_BATTLE_BLOCK_SET_EVENT).getInvoker().preBattleBlockSet(pos, state, Optional.ofNullable(old), tracer)) {
            if (old != null) {
                old.deinit(tracer);
            }
            specialBlocks.put(pos, factory.create(state, pos, tracer));
            state.getEventMap().getEvent(CoreBattleEvents.POST_BATTLE_BLOCK_SET_EVENT).getInvoker().postBattleBlockSet(pos, state, Optional.ofNullable(old), tracer);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeBattleBlock(final BlockPos pos, final Tracer<ActionTrace> tracer) {
        checkSetup();
        if (!(min.getX() <= pos.getX() && min.getY() <= pos.getY() && min.getZ() <= pos.getZ() &&
                pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ())) {
            return false;
        }
        final BattleEnvironmentBlock old = specialBlocks.get(pos);
        if (old == null) {
            return false;
        }
        if (state.getEventMap().getEvent(CoreBattleEvents.PRE_BATTLE_BLOCK_SET_EVENT).getInvoker().preBattleBlockSet(pos, state, Optional.of(old), tracer)) {
            old.deinit(tracer);
            specialBlocks.remove(pos);
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
        if (!(min.getX() <= pos.getX() && min.getY() <= pos.getY() && min.getZ() <= pos.getZ() &&
                pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ())) {
            return outOfBoundsState;
        }
        final BlockPos realPos = pos.subtract(min).add(realOrigin);
        return delegate.getBlockState(realPos);
    }

    @Override
    public RegistryEntry<Biome> getBiome(final BlockPos pos) {
        checkSetup();
        if (!(min.getX() <= pos.getX() && min.getY() <= pos.getY() && min.getZ() <= pos.getZ() &&
                pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ())) {
            return outOfBoundsBiome;
        }
        final BlockPos realPos = pos.subtract(min).add(realOrigin);
        return delegate.getBiome(realPos);
    }

    @Override
    public Optional<BattleEnvironmentBlock> getBattleBlock(final BlockPos pos) {
        checkSetup();
        if (!(min.getX() <= pos.getX() && min.getY() <= pos.getY() && min.getZ() <= pos.getZ() &&
                pos.getX() <= max.getX() && pos.getY() <= max.getY() && pos.getZ() <= max.getZ())) {
            return Optional.empty();
        }
        return Optional.ofNullable(specialBlocks.get(pos));
    }

    @Override
    public boolean checkForStanding(final BattleParticipantBounds bounds, final BlockPos pos, final boolean onGround) {
        final BattleParticipantBounds move = BattleParticipantBounds.move(pos, bounds);
        final Iterator<BattleParticipantBounds.Part> iterator = move.parts();
        boolean foundFloor = false;
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
                        final VoxelShape shape = state.getCollisionShape(delegate, pos);
                        if (VoxelShapes.matchesAnywhere(collisionShape, shape, BooleanBiFunction.AND)) {
                            return false;
                        }
                        if (onGround && !foundFloor) {
                            if (VoxelShapes.matchesAnywhere(floorShape, shape, BooleanBiFunction.AND)) {
                                foundFloor = true;
                            }
                        }
                    }
                }
            }
        }
        return !onGround || foundFloor;
    }

    @Override
    public BlockView asBlockView() {
        return delegate;
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

        public BattleEnvironment create(final World delegate) {
            return new BattleEnvironmentImpl(outOfBoundsState, outOfBoundsBiome, min, max, delegate);
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
