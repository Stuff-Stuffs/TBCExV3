package io.github.stuff_stuffs.tbcexv3core.impl.event;

import io.github.stuff_stuffs.tbcexv3core.api.event.EventKey;
import io.github.stuff_stuffs.tbcexv3core.api.event.InvokerFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.function.Function;

public class SortedEventImpl<ViewType, EventType> extends EventImpl<ViewType, EventType> {
    private final Comparator<? super EventType> comparator;

    public SortedEventImpl(final EventKey<ViewType, EventType> key, final Function<ViewType, EventType> converter, final InvokerFactory<EventType> invokerFactory, final Runnable enter, final Runnable exit, final Comparator<? super EventType> comparator) {
        super(key, converter, invokerFactory, enter, exit);
        this.comparator = comparator;
    }

    @Override
    public EventType getInvoker() {
        if (invoker == null) {
            final ObjectCollection<EventType> values = map.values();
            final EventType[] listeners = values.toArray(i -> (EventType[]) Array.newInstance(key.getMutClass(), i));
            ObjectArrays.stableSort(listeners, comparator);
            invoker = invokerFactory.createInvoker(listeners, enter, exit);
        }
        return invoker;
    }
}
