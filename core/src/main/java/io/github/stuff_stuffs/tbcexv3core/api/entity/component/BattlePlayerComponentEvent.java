package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;

public interface BattlePlayerComponentEvent {
    Event<BattlePlayerComponentEvent> EVENT = EventFactory.createArrayBacked(BattlePlayerComponentEvent.class, battlePlayerComponentEvents -> (entity, builder) -> {
        for (BattlePlayerComponentEvent event : battlePlayerComponentEvents) {
            event.onStateBuilder(entity, builder);
        }
    });

    void onStateBuilder(PlayerEntity entity, BattleParticipantStateBuilder builder);
}
