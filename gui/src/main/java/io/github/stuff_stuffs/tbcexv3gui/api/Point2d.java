package io.github.stuff_stuffs.tbcexv3gui.api;

import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;

public record Point2d(double x, double y) {
    public Point2d combine(Point2d other, DoubleBinaryOperator operator) {
        return new Point2d(operator.applyAsDouble(x, other.x), operator.applyAsDouble(y, other.y));
    }
}
