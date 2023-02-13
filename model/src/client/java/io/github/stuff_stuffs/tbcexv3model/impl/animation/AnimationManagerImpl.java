package io.github.stuff_stuffs.tbcexv3model.impl.animation;

import io.github.stuff_stuffs.tbcexv3model.api.animation.AnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.animation.ModelAnimationManager;
import io.github.stuff_stuffs.tbcexv3model.api.model.ModelAnimationContext;
import io.github.stuff_stuffs.tbcexv3model.api.scene.AnimationScene;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Map;

public class AnimationManagerImpl<T> implements AnimationManager<T> {
    private final AnimationScene scene = AnimationScene.create();
    private final Map<Identifier, ModelAnimationManagerImpl<T>> forModels = new Object2ReferenceOpenHashMap<>();

    @Override
    public AnimationScene scene() {
        return scene;
    }

    @Override
    public ModelAnimationManager<T> forModel(final Identifier id) {
        return forModels.computeIfAbsent(id, i -> new ModelAnimationManagerImpl<>());
    }

    @Override
    public void update(final double time, final T data) {
        final ModelAnimationContext<T> context = new ModelAnimationContext<T>() {
            @Override
            public T context() {
                return data;
            }

            @Override
            public AnimationScene scene() {
                return scene;
            }
        };
        for (final Map.Entry<Identifier, ModelAnimationManagerImpl<T>> entry : forModels.entrySet()) {
            entry.getValue().update(scene.getModel(entry.getKey()), context, time);
        }
    }

    @Override
    public void close() throws Exception {
        scene.close();
    }
}
