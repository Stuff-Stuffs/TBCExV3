package io.github.stuff_stuffs.tbcexv3model.impl.animation;

import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelIdleAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelAnimationContext;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class ModelAnimationManagerImpl<T> implements ModelAnimationManager<T> {
    private ModelIdleAnimation<T> idle;
    private final ObjectArrayFIFOQueue<Listener<T>> queue = new ObjectArrayFIFOQueue<>();
    private ModelAnimation<T> current = null;
    private double lastAnimationTime = 0;
    private double startTime = 0;

    @Override
    public void setIdleAnimation(final ModelIdleAnimation<T> modelAnimation) {
        idle = modelAnimation;
    }

    @Override
    public boolean open() {
        return current == null;
    }

    @Override
    public boolean setAnimation(final ModelAnimation<T> animation) {
        if (open()) {
            current = animation;
            return true;
        }
        return false;
    }

    @Override
    public void enqueueListener(final Listener<T> listener) {
        queue.enqueue(listener);
    }

    public void update(final Model model, final ModelAnimationContext<T> context, final double time) {
        if (open()) {
            while (!queue.isEmpty() && open()) {
                final MutableBoolean canceled = new MutableBoolean(false);
                queue.first().onOpen(this::setAnimation, canceled::setTrue);
                if (canceled.booleanValue() || !open()) {
                    queue.dequeue();
                } else {
                    break;
                }
            }
            if (!open()) {
                startTime = time;
            }
        }
        if (open()) {
            if (idle != null) {
                idle.animate(model, context, time, time - lastAnimationTime);
            }
        } else {
            if (current.animate(model, context, time, startTime)) {
                current = null;
            }
            lastAnimationTime = time;
        }
    }
}
