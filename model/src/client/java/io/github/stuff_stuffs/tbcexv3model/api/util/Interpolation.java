package io.github.stuff_stuffs.tbcexv3model.api.util;

public interface Interpolation {
    double remapAlpha(double a);

    static Interpolation blend(final Interpolation first, final Interpolation second, final double alpha) {
        return a -> first.remapAlpha(a) * (1 - alpha) + second.remapAlpha(a) * alpha;
    }

    static Interpolation linear() {
        return a -> a;
    }
}
