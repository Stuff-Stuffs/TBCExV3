package io.github.stuff_stuffs.tbcexv3gui.api.util;

import io.github.stuff_stuffs.tbcexv3gui.api.widget.Axis;

import java.util.function.DoubleBinaryOperator;

public record Point2d(double x, double y) implements Comparable<Point2d> {
    public Point2d combine(final Point2d other, final DoubleBinaryOperator operator) {
        return new Point2d(operator.applyAsDouble(x, other.x), operator.applyAsDouble(y, other.y));
    }

    public Point2d convert(final Axis axis) {
        return switch (axis) {
            case X -> this;
            case Y -> new Point2d(y, x);
        };
    }

    @Override
    public int compareTo(final Point2d o) {
        final int i = Double.compare(x, o.x);
        if (i != 0) {
            return i;
        }
        return Double.compare(y, o.y);
    }
}
