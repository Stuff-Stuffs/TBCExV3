package io.github.stuff_stuffs.tbcexv3core.api.event;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface EventView<View> {
    EventKey<View, ?> getKey();

    EventListenerKey registerListener(View listener);
}
