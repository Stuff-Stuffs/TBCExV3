package io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event;

import io.github.stuff_stuffs.tbcexv3core.impl.event.EventKeyImpl;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface EventKey<ViewType, EventType> {
    Identifier getId();

    Class<ViewType> getViewClass();

    Class<EventType> getMutClass();

    static <ViewType, EventType> EventKey<ViewType, EventType> create(final Identifier id, final Class<ViewType> viewClass, final Class<EventType> mutClass) {
        return EventKeyImpl.create(id, viewClass, mutClass);
    }
}
