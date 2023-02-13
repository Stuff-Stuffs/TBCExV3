package io.github.stuff_stuffs.tbcexv3model.impl.animation;

import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationFactory;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.animation.SceneAnimation;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelAnimationContext;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class AnimationManagerImpl<T> implements AnimationManager<T> {
    private final Map<Identifier, ModelAnimationManagerImpl<T>> forModels = new Object2ReferenceOpenHashMap<>();
    private double lastTime;
    private double startTime;
    private @Nullable SceneAnimation<T> current = null;
    private Queue<SceneAnimation<T>> animations = new ArrayDeque<>();

    @Override
    public ModelAnimationManager<T> forModel(final Identifier id) {
        return forModels.get(id);
    }

    public void addModel(final Identifier id, final ModelType type, final Map<Identifier, ModelAnimationFactory<T>> factories) {
        forModels.put(id, new ModelAnimationManagerImpl<>(type, factories));
    }

    @Override
    public void update(final double time, final T data, final AnimationScene<T> scene) {
        lastTime = time;
        final ModelAnimationContext<T> context = new ModelAnimationContext<T>() {
            @Override
            public T context() {
                return data;
            }

            @Override
            public AnimationScene<T> scene() {
                return scene;
            }
        };
        for (final Map.Entry<Identifier, ModelAnimationManagerImpl<T>> entry : forModels.entrySet()) {
            entry.getValue().update(scene.getModel(entry.getKey()), context, time);
        }
        if(current==null && !animations.isEmpty()) {
            startTime = lastTime;
            current = animations.remove();
        }
        if(current!=null) {
            if(current.update(scene, time, startTime)) {
                current = null;
            }
        }
    }

    @Override
    public void enqueueAnimation(final SceneAnimation<T> sceneAnimation) {
        if (current == null) {
            startTime = lastTime;
            current = sceneAnimation;
        } else {
            animations.add(sceneAnimation);
        }
    }
}
