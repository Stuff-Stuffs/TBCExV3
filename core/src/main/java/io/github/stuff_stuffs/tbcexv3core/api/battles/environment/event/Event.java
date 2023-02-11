package io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface Event<ViewType, EventType> extends EventView<ViewType> {
    @Override
    EventKey<ViewType, EventType> getKey();

    EventListenerKey registerListenerMut(EventType listener);

    EventType getInvoker();
}
