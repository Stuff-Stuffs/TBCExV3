package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;

public interface StateUpdater<T> {
    boolean event(WidgetEvent event, T data);

    default void updateBounds(final Rectangle bounds, final T data) {
    }

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
