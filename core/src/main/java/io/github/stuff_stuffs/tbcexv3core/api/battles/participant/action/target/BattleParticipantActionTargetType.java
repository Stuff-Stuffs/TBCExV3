package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.action.target.BattleParticipantActionTargetTypeImpl;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantActionTargetType<T extends BattleParticipantActionTarget> {
    Class<T> targetClass();

    static <T extends BattleParticipantActionTarget> BattleParticipantActionTargetType<T> of(final Identifier identifier, final Class<T> clazz) {
        return BattleParticipantActionTargetTypeImpl.of(identifier, clazz);
    }
}