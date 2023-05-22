package io.github.stuff_stuffs.tbcexv3util.api.util.event;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface EventListenerKey {
    boolean exists();

    void destroy();
}
