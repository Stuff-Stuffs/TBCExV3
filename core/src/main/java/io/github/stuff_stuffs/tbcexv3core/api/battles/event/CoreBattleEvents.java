package io.github.stuff_stuffs.tbcexv3core.api.battles.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.event.team.PostChangeTeamRelationEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.team.PreChangeTeamRelationEvent;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventKey;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;

public final class CoreBattleEvents {
    public static final EventKey<PreBattleSetBoundsEvent.View, PreBattleSetBoundsEvent> PRE_BATTLE_SET_BOUNDS_EVENT = EventKey.create(TBCExV3Core.createId("pre_battle_bounds_set"), PreBattleSetBoundsEvent.View.class, PreBattleSetBoundsEvent.class);
    public static final EventKey<PostBattleBoundsSetEvent.View, PostBattleBoundsSetEvent> POST_BATTLE_SET_BOUNDS_EVENT = EventKey.create(TBCExV3Core.createId("post_battle_bounds_set"), PostBattleBoundsSetEvent.View.class, PostBattleBoundsSetEvent.class);
    public static final EventKey<PreBattleParticipantJoinEvent.View, PreBattleParticipantJoinEvent> PRE_BATTLE_PARTICIPANT_JOIN_EVENT = EventKey.create(TBCExV3Core.createId("pre_battle_participant_join"), PreBattleParticipantJoinEvent.View.class, PreBattleParticipantJoinEvent.class);
    public static final EventKey<PostBattleParticipantJoinEvent.View, PostBattleParticipantJoinEvent> POST_BATTLE_PARTICIPANT_JOIN_EVENT = EventKey.create(TBCExV3Core.createId("post_battle_participant_join"), PostBattleParticipantJoinEvent.View.class, PostBattleParticipantJoinEvent.class);
    public static final EventKey<PreBattleParticipantLeaveEvent.View, PreBattleParticipantLeaveEvent> PRE_BATTLE_PARTICIPANT_LEAVE_EVENT = EventKey.create(TBCExV3Core.createId("pre_battle_participant_leave"), PreBattleParticipantLeaveEvent.View.class, PreBattleParticipantLeaveEvent.class);
    public static final EventKey<PostBattleParticipantLeaveEvent.View, PostBattleParticipantLeaveEvent> POST_BATTLE_PARTICIPANT_LEAVE_EVENT = EventKey.create(TBCExV3Core.createId("post_battle_participant_leave"), PostBattleParticipantLeaveEvent.View.class, PostBattleParticipantLeaveEvent.class);
    public static final EventKey<PreBattleEndEvent.View, PreBattleEndEvent> PRE_BATTLE_END_EVENT = EventKey.create(TBCExV3Core.createId("pre_end"), PreBattleEndEvent.View.class, PreBattleEndEvent.class);
    public static final EventKey<PostBattleEndEvent.View, PostBattleEndEvent> POST_BATTLE_END_EVENT = EventKey.create(TBCExV3Core.createId("post_end"), PostBattleEndEvent.View.class, PostBattleEndEvent.class);
    public static final EventKey<PreChangeTeamRelationEvent.View, PreChangeTeamRelationEvent> PRE_TEAM_RELATION_CHANGE_EVENT = EventKey.create(TBCExV3Core.createId("pre_team_relation_change"), PreChangeTeamRelationEvent.View.class, PreChangeTeamRelationEvent.class);
    public static final EventKey<PostChangeTeamRelationEvent.View, PostChangeTeamRelationEvent> POST_TEAM_RELATION_CHANGE_EVENT = EventKey.create(TBCExV3Core.createId("post_team_relation_change"), PostChangeTeamRelationEvent.View.class, PostChangeTeamRelationEvent.class);

    private CoreBattleEvents() {
    }
}
