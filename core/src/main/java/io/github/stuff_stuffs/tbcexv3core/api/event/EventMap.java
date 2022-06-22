package io.github.stuff_stuffs.tbcexv3core.api.event;

import io.github.stuff_stuffs.tbcexv3core.impl.event.EventMapImpl;
import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.function.Function;

@ApiStatus.NonExtendable
public interface EventMap extends EventMapView {
    <ViewType, EventType> Event<ViewType, EventType> getEvent(EventKey<ViewType, EventType> key);

    interface Builder {
        boolean contains(EventKey<?, ?> key);

        <ViewType, EventType> Builder unsorted(EventKey<ViewType, EventType> key, Function<ViewType, EventType> converter, InvokerFactory<EventType> invokerFactory);

        <ViewType, EventType> Builder sorted(EventKey<ViewType, EventType> key, Function<ViewType, EventType> converter, InvokerFactory<EventType> invokerFactory, Comparator<? super EventType> comparator);

        EventMap build();

        static Builder create() {
            return new EventMapImpl.BuilderImpl();
        }
    }
}
