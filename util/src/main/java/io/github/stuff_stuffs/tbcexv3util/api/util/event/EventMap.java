package io.github.stuff_stuffs.tbcexv3util.api.util.event;

import io.github.stuff_stuffs.tbcexv3util.impl.event.EventMapImpl;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface EventMap extends EventMapView {
    <ViewType, EventType> Event<ViewType, EventType> getEvent(EventKey<ViewType, EventType> key);

    interface Builder {
        boolean contains(EventKey<?, ?> key);

        <ViewType, EventType> Builder add(EventKey<ViewType, EventType> key);

        EventMap build();

        static Builder create() {
            return new EventMapImpl.BuilderImpl();
        }
    }
}
