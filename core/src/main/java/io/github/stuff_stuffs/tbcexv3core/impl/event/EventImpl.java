package io.github.stuff_stuffs.tbcexv3core.impl.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event.EventKey;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event.EventListenerKey;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event.Event;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event.InvokerFactory;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;

import java.lang.reflect.Array;
import java.util.function.Function;

public class EventImpl<ViewType, EventType> implements Event<ViewType, EventType> {
    protected final EventKey<ViewType, EventType> key;
    protected final Function<ViewType, EventType> converter;
    protected final InvokerFactory<EventType> invokerFactory;
    protected final Reference2ObjectLinkedOpenHashMap<EventListenerKeyImpl, EventType> map;
    protected final Runnable enter;
    protected final Runnable exit;
    protected EventType invoker;

    public EventImpl(final EventKey<ViewType, EventType> key, final Function<ViewType, EventType> converter, final InvokerFactory<EventType> invokerFactory, final Runnable enter, final Runnable exit) {
        this.key = key;
        this.converter = converter;
        this.invokerFactory = invokerFactory;
        this.enter = enter;
        this.exit = exit;
        map = new Reference2ObjectLinkedOpenHashMap<>();
    }

    @Override
    public EventKey<ViewType, EventType> getKey() {
        return key;
    }

    @Override
    public EventListenerKey registerListener(final ViewType listener) {
        final EventListenerKeyImpl key = new EventListenerKeyImpl(this);
        map.putAndMoveToLast(key, converter.apply(listener));
        invoker = null;
        return key;
    }

    @Override
    public EventListenerKey registerListenerMut(final EventType listener) {
        final EventListenerKeyImpl key = new EventListenerKeyImpl(this);
        map.putAndMoveToLast(key, listener);
        invoker = null;
        return key;
    }

    @Override
    public EventType getInvoker() {
        if (invoker == null) {
            final ObjectCollection<EventType> values = map.values();
            invoker = invokerFactory.createInvoker(values.toArray(i -> (EventType[]) Array.newInstance(key.getMutClass(), i)), enter, exit);
        }
        return invoker;
    }

    public void remove(final EventListenerKeyImpl key) {
        if (key.getOwner() == this) {
            map.remove(key);
            invoker = null;
        }
    }
}
