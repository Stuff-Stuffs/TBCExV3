package io.github.stuff_stuffs.tbcexv3core.internal.common.world;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.SortedSet;

public class BattleDisplayWorldContainer {
    private static final int PADDING = 256;
    private final SortedSet<Interval> freeSpace;
    private final Map<BattleHandle, Interval> reservedSpace;
    private final ServerWorld world;
    private final int startX;
    private final int startY;
    private final int startZ;

    public BattleDisplayWorldContainer(final ServerWorld world, final int maxX, final int startX, final int startY, final int startZ) {
        this.world = world;
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        freeSpace = new ObjectAVLTreeSet<>();
        reservedSpace = new Object2ReferenceOpenHashMap<>();
        freeSpace.add(new Interval(startX, maxX));
    }

    public BlockPos allocate(final BattleHandle handle, final int maxWidth) {
        Interval best = null;
        for (final Interval interval : freeSpace) {
            if (interval.endX - interval.startX >= maxWidth + PADDING && (best == null || interval.endX - interval.startX < best.endX - best.startX)) {
                best = interval;
            }
        }
        if (best == null) {
            throw new RuntimeException("Could not allocate space for a battle!");
        }
        freeSpace.remove(best);
        final Interval reserved = new Interval(best.startX, best.startX + maxWidth + PADDING);
        if (best.startX + maxWidth + PADDING + 1 != best.endX) {
            final Interval rest = new Interval(best.startX + maxWidth + PADDING, best.endX);
            freeSpace.add(rest);
        }
        reservedSpace.put(handle, reserved);
        return new BlockPos(reserved.startX, startY, startZ);
    }

    public void deallocate(final BattleHandle handle) {
        final Interval removed = reservedSpace.remove(handle);
        final SortedSet<Interval> head = freeSpace.headSet(removed);
        final SortedSet<Interval> tail = freeSpace.tailSet(removed);
        final Interval previous = head.isEmpty() ? null : head.last();
        final Interval next = tail.isEmpty() ? null : tail.first();
        final boolean removePrevious = previous != null && previous.endX == removed.startX;
        final boolean removeNext = next != null && next.startX == removed.endX;
        final Interval released = new Interval(removePrevious ? previous.startX : removed.startX, removeNext ? next.endX : removed.endX);
        if (removePrevious) {
            freeSpace.remove(previous);
        }
        if (removeNext) {
            freeSpace.remove(next);
        }
        freeSpace.add(released);
        final int width = (removed.endX - removed.startX - PADDING + 15) >> 4;
        TBCExV3Core.unApply(world, new BlockPos(removed.startX, startY, startZ), width, width);
    }

    private record Interval(int startX, int endX) implements Comparable<Interval> {
        @Override
        public int compareTo(final BattleDisplayWorldContainer.Interval o) {
            final int i = Integer.compare(startX, o.startX);
            if (i != 0) {
                return i;
            }
            return Integer.compare(endX, o.endX);
        }
    }
}
