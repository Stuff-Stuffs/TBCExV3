package io.github.stuff_stuffs.tbcexv3_gui.api.util;

public interface Quadrilateral {
    default Point2d getVertex(final int vertexIndex) {
        return new Point2d(getVertexX(vertexIndex), getVertexY(vertexIndex));
    }

    double getVertexX(int vertexIndex);

    double getVertexY(int vertexIndex);

    default Point2d center() {
        Point2d c = new Point2d(0, 0);
        for (int i = 0; i < 4; i++) {
            c = c.sum(getVertex(i));
        }
        return c.combine(0.25, (x, y) -> x * y);
    }
}
