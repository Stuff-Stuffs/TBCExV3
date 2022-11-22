package io.github.stuff_stuffs.tbcexv3_gui.api.util;

import io.github.stuff_stuffs.tbcexv3_gui.api.widget.Axis;

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

    public Point2d combine(final double v, final DoubleBinaryOperator operator) {
        return new Point2d(operator.applyAsDouble(x, v), operator.applyAsDouble(y, v));
    }

    public Point2d sum(final Point2d o) {
        return combine(o, Double::sum);
    }

    public Point2d add(final double a) {
        return sum(new Point2d(a, a));
    }

    public double pos(final Axis axis) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
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
