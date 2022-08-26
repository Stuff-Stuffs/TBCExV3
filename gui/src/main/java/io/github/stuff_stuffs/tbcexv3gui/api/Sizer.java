package io.github.stuff_stuffs.tbcexv3gui.api;

public interface Sizer<T> {
    Rectangle calculateSize(T data, Rectangle min, Rectangle max);

    static <T> Sizer<T> min() {
        return (data, min, max) -> min;
    }

    static <T> Sizer<T> max() {
        return (data, min, max) -> max;
    }
}
