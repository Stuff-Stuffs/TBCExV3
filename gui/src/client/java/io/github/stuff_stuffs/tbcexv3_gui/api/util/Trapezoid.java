package io.github.stuff_stuffs.tbcexv3_gui.api.util;

import net.fabricmc.fabric.api.util.TriState;

public final class Trapezoid implements Quadrilateral {
    private final Point2d[] points;

    public Trapezoid(final double innerRadius, final double outerRadius, final double startAngle, final double endAngle, final Point2d radialCenter) {
        points = new Point2d[4];
        final double startSin = Math.sin(startAngle);
        final double startCos = Math.cos(startAngle);
        final double endSin = Math.sin(endAngle);
        final double endCos = Math.cos(endAngle);
        points[0] = new Point2d(startSin * innerRadius, startCos * innerRadius).combine(radialCenter, Double::sum);
        points[1] = new Point2d(startSin * outerRadius, startCos * outerRadius).combine(radialCenter, Double::sum);
        points[2] = new Point2d(endSin * outerRadius, endCos * outerRadius).combine(radialCenter, Double::sum);
        points[3] = new Point2d(endSin * innerRadius, endCos * innerRadius).combine(radialCenter, Double::sum);
    }

    public Rectangle getBounds() {
        return new Rectangle(
                new Point2d(
                        Math.min(Math.min(points[0].x(), points[1].x()), Math.min(points[2].x(), points[3].x())),
                        Math.min(Math.min(points[0].y(), points[1].y()), Math.min(points[2].y(), points[3].y()))
                ),
                new Point2d(
                        Math.max(Math.max(points[0].x(), points[1].x()), Math.max(points[2].x(), points[3].x())),
                        Math.max(Math.max(points[0].y(), points[1].y()), Math.max(points[2].y(), points[3].y()))
                )
        );
    }

    public boolean isIn(final double x, final double y) {
        TriState gz = TriState.DEFAULT;
        for (int i = 0; i < 4; i++) {
            final Point2d first = points[i];
            final Point2d second = points[(i + 1) & 3];
            final double s = (y - first.y()) * (second.x() - first.x()) - (x - first.x()) * (second.y() - first.y());
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

    @Override
    public Point2d getVertex(final int vertexIndex) {
        return points[vertexIndex & 3];
    }

    @Override
    public double getVertexX(final int vertexIndex) {
        return points[vertexIndex & 3].x();
    }

    @Override
    public double getVertexY(final int vertexIndex) {
        return points[vertexIndex & 3].y();
    }
}
