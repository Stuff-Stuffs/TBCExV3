package io.github.stuff_stuffs.tbcexv3core.impl.battles;

import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironment;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentBlock;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.internal.common.environment.BattleEnvironmentSection;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public class ClientBattleEnvironmentImpl implements BattleEnvironment {
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

    public ClientBattleEnvironmentImpl(final BlockState outOfBoundsState, final RegistryEntry<Biome> outOfBoundsBiome, final BlockPos min, final BlockPos max, final BattleEnvironmentSection[] sections) {
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
    public BlockPos origin() {
        return min;
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
}
