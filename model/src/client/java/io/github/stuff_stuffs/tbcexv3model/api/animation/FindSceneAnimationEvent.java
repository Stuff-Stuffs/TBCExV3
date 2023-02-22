package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface FindSceneAnimationEvent {
    Identifier SETUP_ANIMATION_ID = TBCExV3Model.id("setup");
    Event<FindSceneAnimationEvent> EVENT = EventFactory.createArrayBacked(FindSceneAnimationEvent.class, events -> (id, context) -> {
        for (final FindSceneAnimationEvent event : events) {
            final Optional<SceneAnimation> animation = event.find(id, context);
            if (animation.isPresent()) {
                return animation;
            }
        }
        return Optional.empty();
    });

    Optional<SceneAnimation> find(Identifier id, Context<?> context);

    interface Context<T> {
        double time();

        T data();
    }

    static <T> Context<T> createContext(final double time, final T data) {
        return new Context<>() {
            @Override
            public double time() {
                return time;
            }

            @Override
            public T data() {
                return data;
            }
        };
    }
}
