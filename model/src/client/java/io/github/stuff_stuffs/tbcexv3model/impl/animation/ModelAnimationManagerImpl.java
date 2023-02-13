package io.github.stuff_stuffs.tbcexv3model.impl.animation;

import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationFactory;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.model.Model;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelAnimationContext;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ModelAnimationManagerImpl<T> implements ModelAnimationManager<T> {
    private final Map<Identifier, ModelAnimationFactory<T>> animations = new Object2ReferenceOpenHashMap<>();
    private final ModelType type;
    private double lastTime = 0;
    private double startTime = 0;
    private @Nullable ModelAnimation<T> current = null;

    public ModelAnimationManagerImpl(final ModelType type, final Map<Identifier, ModelAnimationFactory<T>> animations) {
        this.type = type;
        this.animations.putAll(animations);
    }

    @Override
    public ModelType type() {
        return type;
    }

    @Override
    public @Nullable ModelAnimationFactory<T> get(final Identifier animationId) {
        return animations.get(animationId);
    }

    @Override
    public void setModelAnimation(final Identifier animationId, final ModelAnimationFactory<T> animation) {
        animations.put(animationId, animation);
    }

    public void update(final Model model, final ModelAnimationContext<T> context, final double time) {
        lastTime = time;
        if (current != null) {
            if (current.animate(model, context, time, startTime)) {
                current = null;
            }
        }
    }

    @Override
    public void animate(final ModelAnimation<T> animation) {
        startTime = lastTime;
        current = animation;
    }

    @Override
    public boolean isDone() {
        return current == null;
    }
}
