package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PostEquipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PostUnequipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PreEquipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PreUnequipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PostGiveBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PostTakeBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PreGiveBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PreTakeBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventKey;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;

public final class CoreBattleParticipantEvents {
    public static final EventKey<PreGiveBattleParticipantItemEvent.View, PreGiveBattleParticipantItemEvent> PRE_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT = EventKey.create(TBCExV3Core.createId("pre_give_participant_item"), PreGiveBattleParticipantItemEvent.View.class, PreGiveBattleParticipantItemEvent.class);
    public static final EventKey<PostGiveBattleParticipantItemEvent.View, PostGiveBattleParticipantItemEvent> POST_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT = EventKey.create(TBCExV3Core.createId("post_give_participant_item"), PostGiveBattleParticipantItemEvent.View.class, PostGiveBattleParticipantItemEvent.class);
    public static final EventKey<PreTakeBattleParticipantItemEvent.View, PreTakeBattleParticipantItemEvent> PRE_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT = EventKey.create(TBCExV3Core.createId("pre_take_participant_item"), PreTakeBattleParticipantItemEvent.View.class, PreTakeBattleParticipantItemEvent.class);
    public static final EventKey<PostTakeBattleParticipantItemEvent.View, PostTakeBattleParticipantItemEvent> POST_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT = EventKey.create(TBCExV3Core.createId("post_take_participant_item"), PostTakeBattleParticipantItemEvent.View.class, PostTakeBattleParticipantItemEvent.class);

    public static final EventKey<PreEquipBattleParticipantEquipmentEvent.View, PreEquipBattleParticipantEquipmentEvent> PRE_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT = EventKey.create(TBCExV3Core.createId("pre_equip_participant_equipment"), PreEquipBattleParticipantEquipmentEvent.View.class, PreEquipBattleParticipantEquipmentEvent.class);
    public static final EventKey<PostEquipBattleParticipantEquipmentEvent.View, PostEquipBattleParticipantEquipmentEvent> POST_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT = EventKey.create(TBCExV3Core.createId("post_equip_participant_equipment"), PostEquipBattleParticipantEquipmentEvent.View.class, PostEquipBattleParticipantEquipmentEvent.class);
    public static final EventKey<PreUnequipBattleParticipantEquipmentEvent.View, PreUnequipBattleParticipantEquipmentEvent> PRE_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT = EventKey.create(TBCExV3Core.createId("pre_unequip_participant_equipment"), PreUnequipBattleParticipantEquipmentEvent.View.class, PreUnequipBattleParticipantEquipmentEvent.class);
    public static final EventKey<PostUnequipBattleParticipantEquipmentEvent.View, PostUnequipBattleParticipantEquipmentEvent> POST_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT = EventKey.create(TBCExV3Core.createId("post_unequip_participant_equipment"), PostUnequipBattleParticipantEquipmentEvent.View.class, PostUnequipBattleParticipantEquipmentEvent.class);

    public static final EventKey<PreBattleParticipantSetTeamEvent.View, PreBattleParticipantSetTeamEvent> PRE_BATTLE_PARTICIPANT_SET_TEAM_EVENT = EventKey.create(TBCExV3Core.createId("pre_participant_set_team"), PreBattleParticipantSetTeamEvent.View.class, PreBattleParticipantSetTeamEvent.class);
    public static final EventKey<PostBattleParticipantSetTeamEvent.View, PostBattleParticipantSetTeamEvent> POST_BATTLE_PARTICIPANT_SET_TEAM_EVENT = EventKey.create(TBCExV3Core.createId("post_participant_set_team"), PostBattleParticipantSetTeamEvent.View.class, PostBattleParticipantSetTeamEvent.class);

    private CoreBattleParticipantEvents() {
    }
}
