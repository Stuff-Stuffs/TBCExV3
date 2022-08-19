package io.github.stuff_stuffs.tbcexv3gui.impl.widget;

import net.minecraft.client.render.RenderLayer;

import java.util.Comparator;

final class DrawnElement {
    static final Comparator<DrawnElement> COMPARATOR = Comparator.<DrawnElement>comparingDouble(element -> element.averageDepth).thenComparingInt(element -> element.matrixState).thenComparingInt(element -> element.scissorState);
    final RenderLayer renderLayer;
    final byte[] data;
    final double averageDepth;
    final int scissorState;
    final int matrixState;

    DrawnElement(final RenderLayer renderLayer, final byte[] data, final double averageDepth, final int scissorState, final int matrixState) {
        this.renderLayer = renderLayer;
        this.data = data;
        this.averageDepth = averageDepth;
        this.scissorState = scissorState;
        this.matrixState = matrixState;
    }
}
