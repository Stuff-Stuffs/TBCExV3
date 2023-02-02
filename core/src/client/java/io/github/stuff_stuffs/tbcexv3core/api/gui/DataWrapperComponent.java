package io.github.stuff_stuffs.tbcexv3core.api.gui;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.util.Observable;

public class DataWrapperComponent<T> extends WrapperComponent<Component> {
    private final Observable<T> data;

    public DataWrapperComponent(final Sizing horizontalSizing, final Sizing verticalSizing, final Component empty, final T data) {
        super(horizontalSizing, verticalSizing, empty);
        this.data = Observable.of(data);
    }

    public Observable<T> getData() {
        return data;
    }
}
