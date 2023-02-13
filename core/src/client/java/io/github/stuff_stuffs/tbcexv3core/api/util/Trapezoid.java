package io.github.stuff_stuffs.tbcexv3core.api.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import org.joml.Math;

public final class Trapezoid {
    private final double[] xs;
    private final double[] ys;
    private final double innerRadius;
    private final double outerRadius;
    private final double angleDelta;

    public Trapezoid(final double innerRadius, final double outerRadius, final double startAngle, final double endAngle) {
        xs = new double[4];
        ys = new double[4];
        final double startSin = Math.sin(startAngle);
        final double startCos = Math.cos(startAngle);
        final double endSin = Math.sin(endAngle);
        final double endCos = Math.cos(endAngle);
        xs[0] = startSin * innerRadius;
        xs[1] = startSin * outerRadius;
        xs[2] = endSin * outerRadius;
        xs[3] = endSin * innerRadius;
        ys[0] = startCos * innerRadius;
        ys[1] = startCos * outerRadius;
        ys[2] = endCos * outerRadius;
        ys[3] = endCos * innerRadius;
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        angleDelta = (endAngle - startAngle);
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public double getOuterRadius() {
        return outerRadius;
    }

    public IntList splitWidths(final int count, final boolean reverse) {
        final IntList list = new IntArrayList();
        final double s = Math.abs(Math.cos(angleDelta * 0.5));
        final double widest = 2 * s * outerRadius;
        final double skinniest = 2 * s * innerRadius;
        final int height = MinecraftClient.getInstance().textRenderer.fontHeight;
        final double start = (widest + skinniest) * 0.5 + height * s * count;
        final double end = (widest + skinniest) * 0.5 - height * s * (count + 1);
        if (!reverse) {
            for (int i = 0; i < count; i++) {
                final double cur = MathHelper.lerp((i + 1) / (double) count, start, end);
                list.add(MathHelper.floor(cur) - 1);
            }
        } else {
            for (int i = 0; i < count; i++) {
                final double cur = Math.min(MathHelper.lerp((count - i) / (double) count, start, end), MathHelper.lerp((count - i - 1) / (double) count, start, end));
                list.add(MathHelper.floor(cur) - 1);
            }
        }
        return list;
    }

    public boolean isIn(final double x, final double y) {
        TriState gz = TriState.DEFAULT;
        for (int i = 0; i < 4; i++) {
            final double cX = xs[i];
            final double cY = ys[i];
            final int next = (i + 1) & 3;
            final double nX = xs[next];
            final double nY = ys[next];
            final double s = (y - cY) * (nX - cX) - (x - cX) * (nY - cY);
            if (gz == TriState.DEFAULT) {
                if (s != 0) {
                    gz = s > 0 ? TriState.TRUE : TriState.FALSE;
                }
            } else {
                if (s != 0) {
                    final TriState cur = s > 0 ? TriState.TRUE : TriState.FALSE;
                    if (cur != gz) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public double x(final int index) {
        return xs[index & 3];
    }

    public double y(final int index) {
        return ys[index & 3];
    }
}
