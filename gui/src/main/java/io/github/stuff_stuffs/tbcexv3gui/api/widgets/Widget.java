package io.github.stuff_stuffs.tbcexv3gui.api.widgets;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;
import io.github.stuff_stuffs.tbcexv3gui.api.RectangleRange;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetEvent;
import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetRenderContext;

public interface Widget<T> {
    void setup(WidgetContext<T> context);

    boolean handleEvent(WidgetEvent events);

    Rectangle resize(RectangleRange range);

    void draw(WidgetRenderContext context);
}
