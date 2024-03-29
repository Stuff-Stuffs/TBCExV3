package io.github.stuff_stuffs.tbcexv3core.api.battles.state;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.BattleEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.environment.BattleEnvironmentView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeam;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.team.BattleParticipantTeamRelation;
import io.github.stuff_stuffs.tbcexv3util.api.util.event.EventMapView;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;
import java.util.stream.Stream;

@ApiStatus.NonExtendable
public interface BattleStateView {
    EventMapView getEventMap();

    BattleStatePhase getPhase();

    BattleStateMode getMode();

    boolean hasEffect(BattleEffectType<?, ?> type);

    <View extends BattleEffect> Optional<View> getEffectView(BattleEffectType<View, ?> type);

    BattleBounds getBattleBounds();

    Iterable<BattleParticipantHandle> getParticipants();

    Stream<BattleParticipantHandle> getParticipantStream();

    BattleParticipantStateView getParticipantByHandle(BattleParticipantHandle handle);

    Optional<BattleParticipantTeam> getTeamById(Identifier id);

    BattleParticipantTeamRelation getTeamRelation(BattleParticipantTeam first, BattleParticipantTeam second);

    Iterable<BattleParticipantHandle> getParticipantsByTeam(BattleParticipantTeam team);

    BattleParticipantTeam getTeamByParticipant(BattleParticipantHandle handle);

    BattleHandle getHandle();

    boolean isCurrentTurn(BattleParticipantHandle handle);

    BattleEnvironmentView getEnvironment();
}
