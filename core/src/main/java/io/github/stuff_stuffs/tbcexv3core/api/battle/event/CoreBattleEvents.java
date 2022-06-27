package io.github.stuff_stuffs.tbcexv3core.api.battle.event;

import io.github.stuff_stuffs.tbcexv3core.api.event.EventKey;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;

public final class CoreBattleEvents {
    public static final EventKey<PreBattleBoundsSet.View, PreBattleBoundsSet> PRE_BATTLE_BOUNDS_SET_EVENT = EventKey.create(TBCExV3Core.createId("pre_battle_bounds_set"), PreBattleBoundsSet.View.class, PreBattleBoundsSet.class);
    public static final EventKey<PostBattleBoundsSet.View, PostBattleBoundsSet> POST_BATTLE_BOUNDS_SET_EVENT = EventKey.create(TBCExV3Core.createId("post_battle_bounds_set"), PostBattleBoundsSet.View.class, PostBattleBoundsSet.class);
    public static final EventKey<PreBattleParticipantJoinEvent.View, PreBattleParticipantJoinEvent> PRE_BATTLE_PARTICIPANT_JOIN_EVENT = EventKey.create(TBCExV3Core.createId("pre_battle_participant_join"), PreBattleParticipantJoinEvent.View.class, PreBattleParticipantJoinEvent.class);
    public static final EventKey<PostBattleParticipantJoinEvent.View, PostBattleParticipantJoinEvent> POST_BATTLE_PARTICIPANT_JOIN_EVENT = EventKey.create(TBCExV3Core.createId("post_battle_participant_join"), PostBattleParticipantJoinEvent.View.class, PostBattleParticipantJoinEvent.class);

    private CoreBattleEvents() {
    }
}
