package io.github.stuff_stuffs.tbcexv3_gui.api.util;

public record Rectangle(Point2d lower, Point2d upper) implements Quadrilateral {
    public Rectangle(final Point2d lower, final Point2d upper) {
        if (lower.x() <= upper.x() && lower.y() <= upper.y()) {
            this.lower = lower;
            this.upper = upper;
        } else {
            this.lower = new Point2d(Math.min(lower.x(), upper.x()), Math.min(lower.y(), upper.y()));
            this.upper = new Point2d(Math.max(lower.x(), upper.x()), Math.max(lower.y(), upper.y()));
        }
    }

    public double width() {
        return upper.x() - lower.x();
    }

    public double height() {
        return upper.y() - lower.y();
    }

    public boolean contains(final Rectangle other) {
        return lower.x() <= other.lower.x() && lower.y() <= other.lower.y() && other.upper.x() <= upper.x() && other.upper.y() <= upper.y();
    }

    public boolean contains(final Point2d point) {
        return lower.x() <= point.x() && lower.y() <= point.y() && point.x() <= upper.x() && point.y() <= upper.y();
    }

    public Rectangle expand(final double expansion) {
        return new Rectangle(new Point2d(lower.x() - expansion, lower.y() - expansion), new Point2d(upper.x() + expansion, upper.y() + expansion));
    }

    public Rectangle union(final Rectangle other) {
        return new Rectangle(lower.combine(other.lower, Double::min), upper.combine(other.upper, Double::max));
    }

    public Rectangle clip(final Rectangle clipper) {
        return new Rectangle(lower.combine(clipper.lower, Math::max), upper.combine(clipper.upper, Math::min));
    }

    @Override
    public double getVertexX(final int vertexIndex) {
        return switch (vertexIndex) {
            case 0, 1 -> lower.x();
            default -> upper.x();
        };
    }

    @Override
    public double getVertexY(final int vertexIndex) {
        return  switch (vertexIndex) {
            case 0, 3 -> lower.y();
            default -> upper.y();
        };
    }
}
