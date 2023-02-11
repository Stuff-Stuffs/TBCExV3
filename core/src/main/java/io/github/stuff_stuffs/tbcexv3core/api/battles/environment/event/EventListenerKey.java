package io.github.stuff_stuffs.tbcexv3core.api.battles.environment.event;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface EventListenerKey {
    boolean exists();

    void destroy();
}
