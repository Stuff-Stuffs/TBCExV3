package io.github.stuff_stuffs.tbcexv3model.api.util;

public interface Transition {
    double alpha(double time);

    static Transition fromInterpolation(final double start, final double duration, final Interpolation interpolation) {
        return time -> {
            final double relative = (time - start) / duration;
            if (duration == 0) {
                return 1;
            }
            if (relative < 0) {
                return 0;
            } else if (relative > 1) {
                return 1;
            }
            return interpolation.remapAlpha(relative);
        };
    }
}
