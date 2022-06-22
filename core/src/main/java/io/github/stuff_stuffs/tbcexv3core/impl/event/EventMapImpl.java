package io.github.stuff_stuffs.tbcexv3core.impl.event;

import io.github.stuff_stuffs.tbcexv3core.api.event.*;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

public class EventMapImpl implements EventMap {
    private final Map<EventKey<?, ?>, Event<?, ?>> map;
    private int depth = 0;

    private EventMapImpl(final Map<EventKey<?, ?>, BuilderImpl.Info<?, ?>> map) {
        this.map = new Object2ReferenceOpenHashMap<>();
        for (final Map.Entry<EventKey<?, ?>, BuilderImpl.Info<?, ?>> entry : map.entrySet()) {
            this.map.put(entry.getKey(), createEvent(entry.getKey(), entry.getValue()));
        }
    }

    private <ViewType, EventType> Event<ViewType, EventType> createEvent(final EventKey<?, ?> key, final BuilderImpl.Info<ViewType, EventType> info) {
        if (info.comparator() == null) {
            return new EventImpl<>((EventKey<ViewType, EventType>) key, info.converter(), info.invokerFactory(), this::enter, this::exit);
        } else {
            return new SortedEventImpl<>((EventKey<ViewType, EventType>) key, info.converter(), info.invokerFactory(), this::enter, this::exit, info.comparator());
        }
    }

    private void enter() {
        depth++;
        if (depth > 512) {
            throw new TBCExException("Probable infinite loop detected!");
        }
    }

    private void exit() {
        depth--;
        if (depth < 0) {
            throw new TBCExException();
        }
    }

    @Override
    public boolean contains(final EventKey<?, ?> key) {
        return map.containsKey(key);
    }

    @Override
    public <View> EventView<View> getEventView(final EventKey<View, ?> key) {
        final Event<View, ?> event = (Event<View, ?>) map.get(key);
        if (event == null) {
            throw new TBCExException();
        }
        return event;
    }

    @Override
    public <ViewType, EventType> Event<ViewType, EventType> getEvent(final EventKey<ViewType, EventType> key) {
        final Event<ViewType, EventType> event = (Event<ViewType, EventType>) map.get(key);
        if (event == null) {
            throw new TBCExException();
        }
        return event;
    }

    public static final class BuilderImpl implements EventMap.Builder {
        private final Map<EventKey<?, ?>, Info<?, ?>> map;

        public BuilderImpl() {
            map = new Object2ReferenceOpenHashMap<>();
        }

        @Override
        public boolean contains(final EventKey<?, ?> key) {
            return map.containsKey(key);
        }

        @Override
        public <ViewType, EventType> Builder unsorted(final EventKey<ViewType, EventType> key, final Function<ViewType, EventType> converter, final InvokerFactory<EventType> invokerFactory) {
            if (map.put(key, new Info<>(converter, invokerFactory, null)) != null) {
                throw new TBCExException("Duplicate event added to EventMap");
            }
            return this;
        }

        @Override
        public <ViewType, EventType> Builder sorted(final EventKey<ViewType, EventType> key, final Function<ViewType, EventType> converter, final InvokerFactory<EventType> invokerFactory, final Comparator<? super EventType> comparator) {
            if (map.put(key, new Info<>(converter, invokerFactory, comparator)) != null) {
                throw new TBCExException("Duplicate event added to EventMap");
            }
            return this;
        }

        @Override
        public EventMap build() {
            return new EventMapImpl(map);
        }

        private record Info<ViewType, EventType>(
                Function<ViewType, EventType> converter,
                InvokerFactory<EventType> invokerFactory,
                @Nullable Comparator<? super EventType> comparator
        ) {
        }
    }
}
