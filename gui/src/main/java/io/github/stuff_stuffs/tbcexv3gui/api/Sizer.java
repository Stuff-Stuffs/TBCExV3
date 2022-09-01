package io.github.stuff_stuffs.tbcexv3gui.api;

public interface Sizer<T> {
    Rectangle calculateSize(T data, RectangleRange range);

    static <T> Sizer<T> min() {
        return (data, range) -> range.getMinRectangle();
    }

    static <T> Sizer<T> max() {
        return (data, range) -> range.getMaxRectangle();
    }
}
