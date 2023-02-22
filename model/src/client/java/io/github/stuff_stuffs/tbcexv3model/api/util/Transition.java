package io.github.stuff_stuffs.tbcexv3model.api.util;

public interface Transition {
    double alpha(double time);

    static Transition fromInterpolation(final double start, final double duration, final Interpolation interpolation) {
        return time -> {
            if (duration <= 0.000001) {
                return 0.5;
            }
            final double relative = (time - start) / duration;
            if (relative < 0) {
                return 0;
            } else if (relative > 1) {
                return 1;
            }
            return interpolation.remapAlpha(relative);
        };
    }
}
