package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action.target;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface BattleParticipantActionTargetType<T> {
    Class<T> targetClass();
}
