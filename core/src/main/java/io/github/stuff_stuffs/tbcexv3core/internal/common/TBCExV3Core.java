package io.github.stuff_stuffs.tbcexv3core.internal.common;

import io.github.stuff_stuffs.tbcexv3core.api.battle.action.CoreBattleActions;
import io.github.stuff_stuffs.tbcexv3core.api.battle.effect.CoreBattleEffects;
import io.github.stuff_stuffs.tbcexv3core.api.battle.event.CoreBattleEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battle.event.PostBattleBoundsSet;
import io.github.stuff_stuffs.tbcexv3core.api.battle.event.PreBattleBoundsSet;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.effect.CoreBattleParticipantEffects;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.CoreBattleParticipantEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.equipment.PostEquipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.equipment.PostUnequipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.equipment.PreEquipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.equipment.PreUnequipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.item.PostGiveBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.item.PostTakeBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.item.PreGiveBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.event.item.PreTakeBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battle.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battle.state.BattleState;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class TBCExV3Core implements ModInitializer {
    public static final String MOD_ID = "tbcexv3_core";

    @Override
    public void onInitialize() {
        CoreBattleActions.init();
        CoreBattleEffects.init();
        CoreBattleParticipantEffects.init();
        BattleState.BATTLE_EVENT_INITIALIZATION_EVENT.register(builder -> {
            builder.unsorted(CoreBattleEvents.PRE_BATTLE_BOUNDS_SET_EVENT, PreBattleBoundsSet::convert, PreBattleBoundsSet::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_BOUNDS_SET_EVENT, PostBattleBoundsSet::convert, PostBattleBoundsSet::invoker);
        });
        BattleParticipantState.BATTLE_PARTICIPANT_EVENT_INITIALIZATION_EVENT.register(builder -> {
            builder.unsorted(CoreBattleParticipantEvents.PRE_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT, PreGiveBattleParticipantItemEvent::convert, PreGiveBattleParticipantItemEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT, PostGiveBattleParticipantItemEvent::convert, PostGiveBattleParticipantItemEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.PRE_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT, PreTakeBattleParticipantItemEvent::convert, PreTakeBattleParticipantItemEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT, PostTakeBattleParticipantItemEvent::convert, PostTakeBattleParticipantItemEvent::invoker);

            builder.unsorted(CoreBattleParticipantEvents.PRE_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT, PreEquipBattleParticipantEquipmentEvent::convert, PreEquipBattleParticipantEquipmentEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT, PostEquipBattleParticipantEquipmentEvent::convert, PostEquipBattleParticipantEquipmentEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.PRE_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT, PreUnequipBattleParticipantEquipmentEvent::convert, PreUnequipBattleParticipantEquipmentEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT, PostUnequipBattleParticipantEquipmentEvent::convert, PostUnequipBattleParticipantEquipmentEvent::invoker);
        });
    }

    public static Identifier createId(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
