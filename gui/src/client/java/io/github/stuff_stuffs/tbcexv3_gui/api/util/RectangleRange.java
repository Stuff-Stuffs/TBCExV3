package io.github.stuff_stuffs.tbcexv3_gui.api.util;

public final class RectangleRange {
    private final Point2d outerLow;
    private final Point2d outerHigh;
    private final Point2d innerLow;
    private final Point2d innerHigh;

    public RectangleRange(final Rectangle exact) {
        this(exact.lower(), exact.lower(), exact.upper(), exact.upper());
    }

    public RectangleRange(final Point2d outerLow, final Point2d innerLow, final Point2d outerHigh, final Point2d innerHigh) {
        if (innerLow.compareTo(innerHigh) > 0 || outerLow.compareTo(outerHigh) > 0) {
            throw new IllegalArgumentException();
        }
        if (outerLow.compareTo(innerLow) > 0 || outerHigh.compareTo(innerHigh) < 0) {
            throw new IllegalArgumentException();
        }
        this.outerLow = outerLow;
        this.outerHigh = outerHigh;
        this.innerLow = innerLow;
        this.innerHigh = innerHigh;
    }

    public Point2d getOuterLow() {
        return outerLow;
    }

    public Point2d getOuterHigh() {
        return outerHigh;
    }

    public Point2d getInnerLow() {
        return innerLow;
    }

    public Point2d getInnerHigh() {
        return innerHigh;
    }

    public Rectangle getMinRectangle() {
        return new Rectangle(innerLow, innerHigh);
    }

    public Rectangle getMaxRectangle() {
        return new Rectangle(outerLow, outerHigh);
    }

    public Point2d getMinExtents() {
        return new Point2d(innerHigh.x() - innerLow.x(), innerHigh.y() - innerLow.y());
    }

    public Point2d getMaxExtents() {
        return new Point2d(outerHigh.x() - outerLow.x(), outerHigh.y() - outerLow.y());
    }

    public RectangleRange clipVertical(final double range, final boolean top) {
        if (top) {
            if (getMaxExtents().y() < range) {
                throw new RuntimeException();
            }
            final Point2d outerHigh = new Point2d(this.outerHigh.x(), this.outerHigh.y() - range);
            final Point2d innerHigh = new Point2d(this.innerHigh.x(), Math.min(outerHigh.y(), getInnerHigh().y()));
            final Point2d innerLow = new Point2d(this.innerLow.x(), Math.min(innerHigh.y(), this.innerLow.y()));
            return new RectangleRange(outerLow, innerLow, outerHigh, innerHigh);
        } else {
            if (getMaxExtents().y() < range) {
                throw new RuntimeException();
            }
            final Point2d outerLow = new Point2d(this.outerLow.x(), this.outerLow.y() + range);
            final Point2d innerLow = new Point2d(this.innerLow.x(), Math.max(outerLow.y(), this.innerLow.y()));
            final Point2d innerHigh = new Point2d(this.innerHigh.x(), Math.max(innerLow.y(), this.innerLow.y()));
            return new RectangleRange(outerLow, innerLow, outerHigh, innerHigh);
        }
    }

    public RectangleRange clipHorizontal(final double range, final boolean left) {
        if (left) {
            if (getMaxExtents().x() < range) {
                throw new RuntimeException();
            }
            final Point2d outerHigh = new Point2d(this.outerHigh.x() - range, this.outerHigh.y());
            final Point2d innerHigh = new Point2d(this.innerHigh.x(), Math.min(outerHigh.y(), getInnerHigh().y()));
            final Point2d innerLow = new Point2d(this.innerLow.x(), Math.min(innerHigh.y(), this.innerLow.y()));
            return new RectangleRange(outerLow, innerLow, outerHigh, innerHigh);
        } else {
            if (getMaxExtents().x() < range) {
                throw new RuntimeException();
            }
            final Point2d outerLow = new Point2d(this.outerLow.x() + range, this.outerLow.y());
            final Point2d innerLow = new Point2d(this.innerLow.x(), Math.max(outerLow.y(), this.innerLow.y()));
            final Point2d innerHigh = new Point2d(this.innerHigh.x(), Math.max(innerLow.y(), this.innerLow.y()));
            return new RectangleRange(outerLow, innerLow, outerHigh, innerHigh);
        }
    }

    public RectangleRange expandExtents(final double width, final double height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException();
        }
        final Point2d other = new Point2d(width, height);
        return new RectangleRange(outerLow, innerLow, outerHigh.sum(other), innerHigh.sum(other));
    }

    public RectangleRange shrinkExtents(final double width, final double height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException();
        }
        final Point2d other = new Point2d(width, height);
        return new RectangleRange(outerLow, innerLow, outerHigh.sum(other).combine(outerLow, Math::max), innerHigh.sum(other).combine(innerLow, Math::max));
    }

    public RectangleRange move(final double x, final double y) {
        final Point2d vec = new Point2d(x, y);
        return new RectangleRange(outerLow.sum(vec), innerLow.sum(vec), outerHigh.sum(vec), innerHigh.sum(vec));
    }

    public static RectangleRange max(final Rectangle rectangle) {
        return new RectangleRange(rectangle.lower(), rectangle.lower(), rectangle.upper(), rectangle.lower());
    }
}
