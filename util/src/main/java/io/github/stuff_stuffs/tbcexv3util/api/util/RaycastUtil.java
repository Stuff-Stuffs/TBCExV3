package io.github.stuff_stuffs.tbcexv3util.api.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class RaycastUtil {
    public static <T> @Nullable T rayCast(final Vec3d start, Vec3d end, final Function<BlockPos, @Nullable T> stopFunction) {
        end = end.add(end.subtract(start).multiply(0.01));
        final int dx = (int) Math.signum(end.x - start.x);
        final double tDeltaX;
        if (dx != 0) {
            if ((end.x - start.x) != 0) {
                tDeltaX = Math.min(dx / (end.x - start.x), Double.MAX_VALUE);
            } else {
                tDeltaX = Double.MAX_VALUE;
            }
        } else {
            tDeltaX = Double.MAX_VALUE;
        }
        double tMaxX;
        if (dx < 0) {
            tMaxX = tDeltaX * MathHelper.fractionalPart(start.x);
        } else {
            tMaxX = tDeltaX * (1 - MathHelper.fractionalPart(start.x));
        }
        int x = MathHelper.floor(start.x);

        final int dy = (int) Math.signum(end.y - start.y);
        final double tDeltaY;
        if (dy != 0) {
            if ((end.y - start.y) != 0) {
                tDeltaY = Math.min(dy / (end.y - start.y), Double.MAX_VALUE);
            } else {
                tDeltaY = Double.MAX_VALUE;
            }
        } else {
            tDeltaY = Double.MAX_VALUE;
        }
        double tMaxY;
        if (dy < 0) {
            tMaxY = tDeltaY * MathHelper.fractionalPart(start.y);
        } else {
            tMaxY = tDeltaY * (1 - MathHelper.fractionalPart(start.y));
        }
        int y = MathHelper.floor(start.y);

        final int dz = (int) Math.signum(end.z - start.z);
        final double tDeltaZ;
        if (dz != 0) {
            if ((end.z - start.z) != 0) {
                tDeltaZ = Math.min(dz / (end.z - start.z), Double.MAX_VALUE);
            } else {
                tDeltaZ = Double.MAX_VALUE;
            }
        } else {
            tDeltaZ = Double.MAX_VALUE;
        }
        double tMaxZ;
        if (dz < 0) {
            tMaxZ = tDeltaZ * MathHelper.fractionalPart(start.z);
        } else {
            tMaxZ = tDeltaZ * (1 - MathHelper.fractionalPart(start.z));
        }
        int z = MathHelper.floor(start.z);
        final BlockPos.Mutable mutable = new BlockPos.Mutable();
        {
            final T stop = stopFunction.apply(mutable.set(x, y, z));
            if (stop != null) {
                return stop;
            }
        }
        while (!(tMaxX > 1 && tMaxY > 1 && tMaxZ > 1)) {
            if (tMaxX < tMaxY) {
                if (tMaxX < tMaxZ) {
                    x += dx;
                    tMaxX += tDeltaX;
                } else {
                    z += dz;
                    tMaxZ += tDeltaZ;
                }
            } else {
                if (tMaxY < tMaxZ) {
                    y += dy;
                    tMaxY += tDeltaY;
                } else {
                    z += dz;
                    tMaxZ += tDeltaZ;
                }
            }
            final T stop = stopFunction.apply(mutable.set(x, y, z));
            if (stop != null) {
                return stop;
            }
        }
        return null;
    }

    private RaycastUtil() {
    }
}
