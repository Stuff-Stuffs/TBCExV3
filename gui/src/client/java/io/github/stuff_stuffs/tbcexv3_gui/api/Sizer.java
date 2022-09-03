package io.github.stuff_stuffs.tbcexv3_gui.api;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;
import io.github.stuff_stuffs.tbcexv3_gui.api.util.RectangleRange;

public interface Sizer<T> {
    Rectangle calculateSize(T data, RectangleRange range);

    static <T> Sizer<T> min() {
        return (data, range) -> range.getMinRectangle();
    }

    static <T> Sizer<T> max() {
        return (data, range) -> range.getMaxRectangle();
    }
}
