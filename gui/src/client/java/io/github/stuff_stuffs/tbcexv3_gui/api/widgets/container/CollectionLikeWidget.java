package io.github.stuff_stuffs.tbcexv3_gui.api.widgets.container;

import io.github.stuff_stuffs.tbcexv3_gui.api.widgets.Widget;
import io.github.stuff_stuffs.tbcexv3_gui.api.widget.WidgetContext;

import java.util.function.Function;

public interface CollectionLikeWidget<T, Handle> extends Widget<T> {
    <K> Handle add(Widget<K> widget, Function<WidgetContext<T>, WidgetContext<K>> contextFactory);
}
