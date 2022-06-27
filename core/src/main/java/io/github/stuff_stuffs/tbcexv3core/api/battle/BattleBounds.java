package io.github.stuff_stuffs.tbcexv3core.api.battle;

import com.mojang.serialization.Codec;
import net.minecraft.util.Util;

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

    public BattleBounds(final int x0, final int y0, final int z0, final int x1, final int y1, final int z1) {
        minX = Math.min(x0, x1);
        minY = Math.min(y0, y1);
        minZ = Math.min(z0, z1);
        maxX = Math.max(x0, x1);
        maxY = Math.max(y0, y1);
        maxZ = Math.max(z0, z1);
    }

    private IntStream stream() {
        return IntStream.of(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
