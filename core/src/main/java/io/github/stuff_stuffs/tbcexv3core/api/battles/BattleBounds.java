package io.github.stuff_stuffs.tbcexv3core.api.battles;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Iterator;
import java.util.stream.IntStream;

public final class BattleBounds {
    public static final Codec<BattleBounds> CODEC = Codec.INT_STREAM.comapFlatMap(stream -> Util.toArray(stream, 6).map(ints -> new BattleBounds(ints[0], ints[1], ints[2], ints[3], ints[4], ints[5])), BattleBounds::stream);
    public static final BattleBounds ZERO_ORIGIN = new BattleBounds(0, 0, 0, 0, 0, 0);
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;

    public BattleBounds(final Box box) {
        minX = MathHelper.floor(box.minX);
        minY = MathHelper.floor(box.minY);
        minZ = MathHelper.floor(box.minZ);
        maxX = MathHelper.floor(box.maxX);
        maxY = MathHelper.floor(box.maxY);
        maxZ = MathHelper.floor(box.maxZ);
    }

    public BattleBounds(final int x0, final int y0, final int z0, final int x1, final int y1, final int z1) {
        minX = Math.min(x0, x1);
        minY = Math.min(y0, y1);
        minZ = Math.min(z0, z1);
        maxX = Math.max(x0, x1);
        maxY = Math.max(y0, y1);
        maxZ = Math.max(z0, z1);
    }

    public boolean isIn(final BattleParticipantBounds bounds) {
        final Iterator<BattleParticipantBounds.Part> parts = bounds.parts();
        while (parts.hasNext()) {
            if (!isIn(parts.next().box())) {
                return false;
            }
        }
        return true;
    }

    public boolean isIn(final Box box) {
        return minX <= box.minX && minY <= box.minY && minZ <= box.minZ
                && box.maxX <= maxX && box.maxY <= maxY && box.minZ <= minZ;
    }

    public boolean isIn(final Vec3i vec) {
        return isIn(vec.getX(), vec.getY(), vec.getZ());
    }

    public boolean isIn(final Vec3d vec) {
        return isIn(vec.x, vec.y, vec.z);
    }

    public boolean isIn(final int x, final int y, final int z) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY && minZ <= z && z <= maxZ;
    }

    public boolean isIn(final double x, final double y, final double z) {
        return minX <= x && x <= maxX && minY <= y && y <= maxY && minZ <= z && z <= maxZ;
    }

    private IntStream stream() {
        return IntStream.of(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
