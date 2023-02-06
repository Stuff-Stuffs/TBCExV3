package io.github.stuff_stuffs.tbcexv3core.impl.event;

import io.github.stuff_stuffs.tbcexv3core.api.event.*;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

public class EventMapImpl implements EventMap {
    private final Map<EventKey<?, ?>, Event<?, ?>> map;
    private final Stack<EventKey<?, ?>> eventStack;
    private int depth = 0;
    private boolean tripped = false;

    private EventMapImpl(final Map<EventKey<?, ?>, BuilderImpl.Info<?, ?>> map) {
        this.map = new Object2ReferenceOpenHashMap<>();
        eventStack = new ObjectArrayList<>();
        for (final Map.Entry<EventKey<?, ?>, BuilderImpl.Info<?, ?>> entry : map.entrySet()) {
            this.map.put(entry.getKey(), createEvent(entry.getKey(), entry.getValue()));
        }
    }

    private <ViewType, EventType> Event<ViewType, EventType> createEvent(final EventKey<?, ?> key, final BuilderImpl.Info<ViewType, EventType> info) {
        if (info.comparator() == null) {
            return new EventImpl<>((EventKey<ViewType, EventType>) key, info.converter(), info.invokerFactory(), () -> enter(key), () -> exit(key));
        } else {
            return new SortedEventImpl<>((EventKey<ViewType, EventType>) key, info.converter(), info.invokerFactory(), () -> enter(key), () -> exit(key), info.comparator());
        }
    }

    private void enter(final EventKey<?, ?> key) {
        final Logger logger = TBCExV3Core.getLogger();
        logger.debug("Entered event: " + key.toString() + ", stack depth: " + depth);
        eventStack.push(key);
        depth++;
        if (depth == 512) {
            logger.warn("Event map depth greater than 512! Probable infinite loop detected!");
            tripped = true;
        }
    }

    private void exit(final EventKey<?, ?> key) {
        depth--;
        final Logger logger = TBCExV3Core.getLogger();
        logger.debug("Exited event: " + key + ", stack depth: " + depth);
        if (eventStack.isEmpty()) {
            logger.error("An event isn't calling exit and enter correctly!");
        } else {
            final EventKey<?, ?> pop = eventStack.pop();
            if (pop != key) {
                logger.error("An event is only calling one of enter or exit!");
            }
        }
        if (depth == 0) {
            if (tripped) {
                tripped = false;
                logger.warn("Looks like that loop wasn't so infinite!");
            }
        } else if (depth < 0) {
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
            return new EventMapImpl(new Object2ReferenceOpenHashMap<>(map));
        }

        private record Info<ViewType, EventType>(
                Function<ViewType, EventType> converter,
                InvokerFactory<EventType> invokerFactory,
                @Nullable Comparator<? super EventType> comparator
        ) {
        }
    }
}
