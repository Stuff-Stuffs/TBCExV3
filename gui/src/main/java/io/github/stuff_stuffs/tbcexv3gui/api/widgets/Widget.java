package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.util.RectangleRange;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;

public interface Widget<T> {
    void setup(WidgetContext<T> context);

    boolean handleEvent(WidgetEvent event);

    Rectangle resize(RectangleRange range);

    void draw(WidgetRenderContext context);
}
