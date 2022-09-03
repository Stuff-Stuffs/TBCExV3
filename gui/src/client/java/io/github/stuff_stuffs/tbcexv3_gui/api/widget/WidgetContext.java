package io.github.stuff_stuffs.tbcexv3_gui.api.widget;

import java.util.function.Function;

public interface WidgetContext<T> {
    void forceResize();

    T getData();

    static <T> WidgetContext<T> root(final T data, final Runnable forceResize) {
        return new WidgetContext<T>() {
            @Override
            public void forceResize() {
                forceResize.run();
            }

            @Override
            public T getData() {
                return data;
            }
        };
    }

    static WidgetContext<Void> stateless(final WidgetContext<?> parentContext) {
        return standalone(parentContext, null);
    }

    static <K> WidgetContext<K> standalone(final WidgetContext<?> parentContext, final K data) {
        return lens(parentContext, p -> data);
    }

    static <T, K> WidgetContext<T> lens(final WidgetContext<K> parentContext, final Function<? super K, ? extends T> lens) {
        return new WidgetContext<T>() {
            @Override
            public void forceResize() {
                parentContext.forceResize();
            }

            @Override
            public T getData() {
                return lens.apply(parentContext.getData());
            }
        };
    }

    static <T> WidgetContext<T> passthrough(final WidgetContext<? extends T> parent) {
        return lens(parent, Function.identity());
    }
}
