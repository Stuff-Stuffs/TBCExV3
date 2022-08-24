package io.github.stuff_stuffs.tbcexv3gui.impl.widget;

import net.minecraft.client.render.RenderLayer;

import java.util.Comparator;

final class DrawnElement {
    static final Comparator<DrawnElement> COMPARATOR = Comparator.<DrawnElement>comparingDouble(element -> element.averageDepth).thenComparingInt(element -> element.drawState);
    final RenderLayer renderLayer;
    final byte[] data;
    final double averageDepth;
    final int drawState;

    DrawnElement(final RenderLayer renderLayer, final byte[] data, final double averageDepth, final int drawState) {
        this.renderLayer = renderLayer;
        this.data = data;
        this.averageDepth = averageDepth;
        this.drawState = drawState;
    }
}
