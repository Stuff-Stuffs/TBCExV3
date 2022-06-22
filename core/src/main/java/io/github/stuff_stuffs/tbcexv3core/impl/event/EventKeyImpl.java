package io.github.stuff_stuffs.tbcexv3core.impl.event;

import io.github.stuff_stuffs.tbcexv3core.api.event.EventKey;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Map;

public record EventKeyImpl<ViewType, EventType>(
        Identifier id,
        Class<ViewType> viewClass,
        Class<EventType> eventClass
) implements EventKey<ViewType, EventType> {
    private static final Map<Info, EventKey<?, ?>> KEY_CACHE = new Object2ReferenceOpenHashMap<>();

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public Class<ViewType> getViewClass() {
        return viewClass;
    }

    @Override
    public Class<EventType> getMutClass() {
        return eventClass;
    }

    public static <ViewType, EventType> EventKey<ViewType, EventType> create(final Identifier id, final Class<ViewType> viewClass, final Class<EventType> eventClass) {
        return (EventKey<ViewType, EventType>) KEY_CACHE.computeIfAbsent(new Info(id, viewClass, eventClass), info -> new EventKeyImpl<>(info.id(), info.view(), info.mut()));
    }

    private record Info(Identifier id, Class<?> view, Class<?> mut) {
    }
}
