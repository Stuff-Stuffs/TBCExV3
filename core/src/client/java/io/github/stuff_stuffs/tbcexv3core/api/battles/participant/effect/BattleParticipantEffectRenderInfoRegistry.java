package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect;

import io.github.stuff_stuffs.tbcexv3core.impl.battles.participant.effect.BattleParticipantEffectRenderInfoRegistryImpl;
import org.jetbrains.annotations.Nullable;

public interface BattleParticipantEffectRenderInfoRegistry {
    BattleParticipantEffectRenderInfoRegistry INSTANCE = new BattleParticipantEffectRenderInfoRegistryImpl();

    <View extends BattleParticipantEffect, Effect extends View> void register(BattleParticipantEffectType<View, Effect> type, BattleParticipantEffectRenderInfo<View, Effect> info);

    <View extends BattleParticipantEffect, Effect extends View> @Nullable BattleParticipantEffectRenderInfo<View, Effect> get(BattleParticipantEffectType<View, Effect> type);
}
