package io.github.stuff_stuffs.tbcexv3core.api.battles.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMapView;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleStateView {
    EventMapView getEventMap();

    BattleStatePhase getPhase();

    BattleStateMode getMode();

    boolean hasEffect(BattleEffectType<?, ?> type);

    <View extends BattleEffect> View getEffectView(BattleEffectType<View, ?> type);

    BattleBounds getBattleBounds();

    Iterable<BattleParticipantHandle> getParticipants();

    BattleParticipantStateView getParticipant(BattleParticipantHandle handle);

    BattleHandle getHandle();
}
