package io.github.stuff_stuffs.tbcexv3model.api.scene;

import io.github.stuff_stuffs.tbcexv3model.api.model.ModelType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface AnimationSceneAddModelEvent {
    Event<AnimationSceneAddModelEvent> EVENT = EventFactory.createArrayBacked(AnimationSceneAddModelEvent.class, events -> (builder, type) -> {
        for (AnimationSceneAddModelEvent event : events) {
            event.onAddModel(builder, type);
        }
    });

    void onAddModel(AnimationScene.ModelBuilder builder, ModelType type);
}
