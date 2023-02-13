package io.github.stuff_stuffs.tbcexv3core.impl.battles.participant.effect;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectRenderInfo;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectRenderInfoRegistry;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BattleParticipantEffectRenderInfoRegistryImpl implements BattleParticipantEffectRenderInfoRegistry {
    private final Map<BattleParticipantEffectType<?, ?>, BattleParticipantEffectRenderInfo<?, ?>> map = new Object2ReferenceOpenHashMap<>();

    @Override
    public <View extends BattleParticipantEffect, Effect extends View> void register(final BattleParticipantEffectType<View, Effect> type, final BattleParticipantEffectRenderInfo<View, Effect> info) {
        if (map.put(type, info) != null) {
            throw new RuntimeException();
        }
    }

    @Override
    public @Nullable <View extends BattleParticipantEffect, Effect extends View> BattleParticipantEffectRenderInfo<View, Effect> get(final BattleParticipantEffectType<View, Effect> type) {
        return (BattleParticipantEffectRenderInfo<View, Effect>) map.get(type);
    }
}
