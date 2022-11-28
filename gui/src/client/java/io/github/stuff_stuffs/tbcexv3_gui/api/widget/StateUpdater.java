package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import io.github.stuff_stuffs.tbcexv3_gui.api.util.Rectangle;

import java.util.function.Function;

public interface StateUpdater<T> {
    boolean event(WidgetEvent event, T data);

    default void updateBounds(final Rectangle bounds, final T data) {
    }

    default StateUpdater<T> compose(final StateUpdater<? super T> stateUpdater) {
        return new StateUpdater<>() {
            @Override
            public boolean event(final WidgetEvent event, final T data) {
                return stateUpdater.event(event, data) || StateUpdater.this.event(event, data);
            }

            @Override
            public void updateBounds(final Rectangle bounds, final T data) {
                stateUpdater.updateBounds(bounds, data);
                StateUpdater.this.updateBounds(bounds, data);
            }
        };
    }

    default StateUpdater<T> andThen(final StateUpdater<? super T> stateUpdater) {
        return new StateUpdater<>() {
            @Override
            public boolean event(final WidgetEvent event, final T data) {
                return StateUpdater.this.event(event, data) || stateUpdater.event(event, data);
            }

            @Override
            public void updateBounds(final Rectangle bounds, final T data) {
                StateUpdater.this.updateBounds(bounds, data);
                stateUpdater.updateBounds(bounds, data);
            }
        };
    }

    default <K> StateUpdater<K> lens(final Function<? super K, ? extends T> lens) {
        return new StateUpdater<>() {
            @Override
            public boolean event(final WidgetEvent event, final K data) {
                return StateUpdater.this.event(event, lens.apply(data));
            }

            @Override
            public void updateBounds(final Rectangle bounds, final K data) {
                StateUpdater.this.updateBounds(bounds, lens.apply(data));
            }
        };
    }

    static <T> StateUpdater<T> none() {
        return new StateUpdater<>() {
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
