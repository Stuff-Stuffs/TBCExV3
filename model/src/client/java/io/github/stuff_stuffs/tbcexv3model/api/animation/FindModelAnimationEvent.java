package io.github.stuff_stuffs.tbcexv3model.api.animation;

import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import io.github.stuff_stuffs.tbcexv3model.internal.common.TBCExV3Model;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.Identifier;

import java.util.Optional;

public interface FindModelAnimationEvent {
    Identifier SETUP_ANIMATION_ID = TBCExV3Model.id("setup");
    Event<FindModelAnimationEvent> EVENT = EventFactory.createArrayBacked(FindModelAnimationEvent.class, events -> (id, context) -> {
        for (FindModelAnimationEvent event : events) {
            final Optional<ModelAnimation> animation = event.find(id, context);
            if (animation.isPresent()) {
                return animation;
            }
        }
        return Optional.empty();
    });

    Optional<ModelAnimation> find(Identifier id, Context<?> context);

    interface Context<T> {
        double time();

        ModelType modelType();

        T data();
    }

    static <T> Context<T> createContext(final double time, final ModelType type, final T data) {
        return new Context<>() {
            @Override
            public double time() {
                return time;
            }

            @Override
            public ModelType modelType() {
                return type;
            }

            @Override
            public T data() {
                return data;
            }
        };
    }
}
