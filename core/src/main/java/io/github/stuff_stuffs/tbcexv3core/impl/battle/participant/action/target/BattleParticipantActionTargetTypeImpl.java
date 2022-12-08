package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTarget;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target.BattleParticipantActionTargetType;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.util.Identifier;

import java.util.Map;

public class BattleParticipantActionTargetTypeImpl<T extends BattleParticipantActionTarget> implements BattleParticipantActionTargetType<T> {
    private static final Map<Identifier, BattleParticipantActionTargetTypeImpl<?>> CACHE = new Object2ReferenceOpenHashMap<>();
    private final Class<T> clazz;

    public BattleParticipantActionTargetTypeImpl(final Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<T> targetClass() {
        return clazz;
    }

    public synchronized static <T extends BattleParticipantActionTarget> BattleParticipantActionTargetType<T> of(final Identifier identifier, final Class<T> clazz) {
        final BattleParticipantActionTargetTypeImpl<?> type = CACHE.get(identifier);
        if (type != null) {
            if (type.targetClass() != clazz) {
                throw new IllegalArgumentException("Type mismatch!");
            }
            return (BattleParticipantActionTargetType<T>) type;
        }
        final BattleParticipantActionTargetTypeImpl<T> newType = new BattleParticipantActionTargetTypeImpl<>(clazz);
        CACHE.put(identifier, newType);
        return newType;
    }
}
