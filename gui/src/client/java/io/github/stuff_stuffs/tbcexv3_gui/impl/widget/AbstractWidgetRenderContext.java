package io.github.stuff_stuffs.tbcexv3_gui.impl.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetRenderContext;

public interface AbstractWidgetRenderContext extends WidgetRenderContext {
    void transformScissors(EmittedQuad quad);

    int createScissorState(int parent, ScissorTransform transform);

    void draw(EmittedQuad quad);
}
