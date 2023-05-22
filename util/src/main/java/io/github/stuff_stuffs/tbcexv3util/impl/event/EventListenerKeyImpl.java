package io.github.stuff_stuffs.tbcexv3util.impl.event;

import io.github.stuff_stuffs.tbcexv3util.api.util.event.EventListenerKey;

public class EventListenerKeyImpl implements EventListenerKey {
    private boolean exists = true;
    private final EventImpl<?, ?> owner;

    public EventListenerKeyImpl(final EventImpl<?, ?> owner) {
        this.owner = owner;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public void destroy() {
        if (exists) {
            exists = false;
            owner.remove(this);
        }
    }

    public EventImpl<?, ?> getOwner() {
        return owner;
    }
}
