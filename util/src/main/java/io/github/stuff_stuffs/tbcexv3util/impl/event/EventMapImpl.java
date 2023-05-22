package io.github.stuff_stuffs.tbcexv3util.impl.event;

import io.github.stuff_stuffs.tbcexv3util.api.util.event.Event;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.EventKey;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.EventMap;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.EventView;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class EventMapImpl implements EventMap {
    public static final Logger LOGGER = LoggerFactory.getLogger("TBCEx.EventMap");
    private final Map<EventKey<?, ?>, Event<?, ?>> map;
    private final Stack<EventKey<?, ?>> eventStack;
    private int depth = 0;
    private boolean tripped = false;

    private EventMapImpl(final Set<EventKey<?, ?>> keys) {
        map = new Object2ReferenceOpenHashMap<>();
        eventStack = new ObjectArrayList<>();
        for (final EventKey<?, ?> key : keys) {
            map.put(key, createEvent(key));
        }
    }

    private <ViewType, EventType> Event<ViewType, EventType> createEvent(final EventKey<ViewType, EventType> key) {
        if (key.comparator() == null) {
            return new EventImpl<>(key, key.converter(), key.invokerFactory(), () -> enter(key), () -> exit(key));
        } else {
            return new SortedEventImpl<>(key, key.converter(), key.invokerFactory(), () -> enter(key), () -> exit(key), key.comparator());
        }
    }

    private void enter(final EventKey<?, ?> key) {
        final Logger logger = LOGGER;
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
        final Logger logger = LOGGER;
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
            throw new RuntimeException();
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
            throw new IllegalArgumentException();
        }
        return event;
    }

    @Override
    public <ViewType, EventType> Event<ViewType, EventType> getEvent(final EventKey<ViewType, EventType> key) {
        final Event<ViewType, EventType> event = (Event<ViewType, EventType>) map.get(key);
        if (event == null) {
            throw new IllegalArgumentException();
        }
        return event;
    }

    public static final class BuilderImpl implements EventMap.Builder {
        private final Set<EventKey<?, ?>> set;

        public BuilderImpl() {
            set = new ObjectOpenHashSet<>();
        }

        @Override
        public boolean contains(final EventKey<?, ?> key) {
            return set.contains(key);
        }

        @Override
        public <EventType, ViewType> Builder add(final EventKey<EventType, ViewType> key) {
            if (!set.add(key)) {
                throw new IllegalArgumentException("Duplicate event added to EventMap");
            }
            return this;
        }

        @Override
        public EventMap build() {
            return new EventMapImpl(set);
        }
    }
}
