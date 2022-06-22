package io.github.stuff_stuffs.tbcexv3core.api.battle.state;

import io.github.stuff_stuffs.tbcexv3core.api.battle.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battle.effect.BattleEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMapView;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleStateView {
    EventMapView getEventMap();

    boolean hasEffect(BattleEffectType<?, ?> type);

    <View extends BattleEffect> View getEffectView(BattleEffectType<View, ?> type);
}
