package io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event;

public interface InvokerFactory<T> {
    T createInvoker(T[] listeners, Runnable enter, Runnable exit);
}
