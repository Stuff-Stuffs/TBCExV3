package io.github.stuff_stuffs.tbcexv3_gui.api.util;

public interface Quadrilateral {
    default Point2d getVertex(final int vertexIndex) {
        return new Point2d(getVertexX(vertexIndex), getVertexY(vertexIndex));
    }

    double getVertexX(int vertexIndex);

    double getVertexY(int vertexIndex);
}
