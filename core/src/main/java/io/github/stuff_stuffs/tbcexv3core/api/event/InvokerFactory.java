package io.github.stuff_stuffs.tbcexv3core.api.event;

public interface InvokerFactory<T> {
    T createInvoker(T[] listeners, Runnable enter, Runnable exit);
}
