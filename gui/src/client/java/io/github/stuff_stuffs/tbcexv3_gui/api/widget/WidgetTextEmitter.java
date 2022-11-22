package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import net.minecraft.text.OrderedText;

public interface WidgetTextEmitter {
    void emit(OrderedText text, double x, double y, int color, boolean shadow);
}
