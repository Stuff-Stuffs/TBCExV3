package io.github.stuff_stuffs.tbcexv3gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3gui.api.widget.WidgetContext;
import io.github.stuff_stuffs.tbcexv3gui.api.widgets.Widget;

import java.util.function.Function;

public interface CollectionLikeWidget<T, Handle> extends Widget<T> {
    <K> Handle add(Widget<K> widget, Function<WidgetContext<T>, WidgetContext<K>> contextFactory);
}
