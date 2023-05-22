package io.github.stuff_stuffs.tbcexv3util.api.util.event;

public interface InvokerFactory<T> {
    T createInvoker(T[] listeners, Runnable enter, Runnable exit);
}