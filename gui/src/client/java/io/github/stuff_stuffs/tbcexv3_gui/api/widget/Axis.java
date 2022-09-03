package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Point2d;

public enum Axis {
    X,
    Y;

    public double choose(final double x, final double y) {
        return switch (this) {
            case X -> x;
            case Y -> y;
        };
    }

    public double choose(final Point2d point) {
        return choose(point.x(), point.y());
    }

    public <T> T choose(final T x, final T y) {
        return switch (this) {
            case X -> x;
            case Y -> y;
        };
    }
}
