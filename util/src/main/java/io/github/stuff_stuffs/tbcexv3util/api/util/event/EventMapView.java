package io.github.stuff_stuffs.tbcexv3util.api.util.event;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface EventMapView {
    boolean contains(EventKey<?, ?> key);

    <View> EventView<View> getEventView(EventKey<View, ?> key);
}
