package io.github.stuff_stuffs.tbcexv3gui.api.widget;

import io.github.stuff_stuffs.tbcexv3gui.api.Rectangle;

public interface StateUpdater<T> {
    boolean event(WidgetEvent event, T data);

    void updateBounds(Rectangle bounds, T data);

    static <T> StateUpdater<T> none() {
        return new StateUpdater<T>() {
            @Override
            public boolean event(final WidgetEvent event, final T data) {
                return false;
            }

            @Override
            public void updateBounds(final Rectangle bounds, final T data) {

            }
        };
    }
}
