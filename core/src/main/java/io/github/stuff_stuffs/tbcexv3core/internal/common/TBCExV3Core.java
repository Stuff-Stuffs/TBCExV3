package io.github.stuff_stuffs.tbcexv3core.internal.common;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleListenerEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.CoreBattleActions;
import io.github.stuff_stuffs.tbcexv3core.api.battles.effect.CoreBattleEffects;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.*;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.team.PostChangeTeamRelationEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.event.team.PreChangeTeamRelationEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.CoreBattleParticipantEffects;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.*;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PostEquipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PostUnequipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PreEquipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.equipment.PreUnequipBattleParticipantEquipmentEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PostGiveBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PostTakeBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PreGiveBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.item.PreTakeBattleParticipantItemEvent;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.*;
import io.github.stuff_stuffs.tbcexv3core.internal.common.mixin.AccessorWorldSavePath;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequestReceiver;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.EntityBattlesUpdateRequestReceiver;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class TBCExV3Core implements ModInitializer {
    public static final String MOD_ID = "tbcexv3_core";
    public static final WorldSavePath TBCEX_WORLD_SAVE_PATH = AccessorWorldSavePath.create("tbcex");
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BattlePlayerComponentEvent.EVENT.register((entity, builder) -> {
            builder.addComponent(TrackedEntityDataComponent.INSTANCE);
            builder.addComponent(new PlayerControlledBattleEntityComponent(entity.getUuid()));
        });
        CoreBattleActions.init();
        CoreBattleEffects.init();
        CoreBattleParticipantEffects.init();
        BattleUpdateRequestReceiver.init();
        EntityBattlesUpdateRequestReceiver.init();
        CoreBattleEntityComponents.init();
        BattleListenerEvent.EVENT.register((view, world) -> view.getState().getEventMap().getEventView(CoreBattleEvents.POST_BATTLE_PARTICIPANT_LEAVE_EVENT).registerListener((state, battleStateView, reason, tracer) -> {
            final Iterator<? extends BattleEntityComponent> iterator = state.entityComponents();
            while (iterator.hasNext()) {
                iterator.next().onLeave(view, world);
            }
        }));
        BattleState.BATTLE_EVENT_INITIALIZATION_EVENT.register(builder -> {
            builder.unsorted(CoreBattleEvents.PRE_BATTLE_SET_BOUNDS_EVENT, PreBattleSetBoundsEvent::convert, PreBattleSetBoundsEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_SET_BOUNDS_EVENT, PostBattleBoundsSetEvent::convert, PostBattleBoundsSetEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_JOIN_EVENT, PreBattleParticipantJoinEvent::convert, PreBattleParticipantJoinEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_PARTICIPANT_JOIN_EVENT, PostBattleParticipantJoinEvent::convert, PostBattleParticipantJoinEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_BATTLE_PARTICIPANT_LEAVE_EVENT, PreBattleParticipantLeaveEvent::convert, PreBattleParticipantLeaveEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_PARTICIPANT_LEAVE_EVENT, PostBattleParticipantLeaveEvent::convert, PostBattleParticipantLeaveEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_BATTLE_END_EVENT, PreBattleEndEvent::convert, PreBattleEndEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_BATTLE_END_EVENT, PostBattleEndEvent::convert, PostBattleEndEvent::invoker);

            builder.unsorted(CoreBattleEvents.PRE_TEAM_RELATION_CHANGE_EVENT, PreChangeTeamRelationEvent::convert, PreChangeTeamRelationEvent::invoker);
            builder.unsorted(CoreBattleEvents.POST_TEAM_RELATION_CHANGE_EVENT, PostChangeTeamRelationEvent::convert, PostChangeTeamRelationEvent::invoker);
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

            builder.unsorted(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_TEAM_EVENT, PreBattleParticipantSetTeamEvent::convert, PreBattleParticipantSetTeamEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_TEAM_EVENT, PostBattleParticipantSetTeamEvent::convert, PostBattleParticipantSetTeamEvent::invoker);

            builder.unsorted(CoreBattleParticipantEvents.PRE_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT, PreBattleParticipantSetBoundsEvent::convert, PreBattleParticipantSetBoundsEvent::invoker);
            builder.unsorted(CoreBattleParticipantEvents.POST_BATTLE_PARTICIPANT_SET_BOUNDS_EVENT, PostBattleParticipantSetBoundsEvent::convert, PostBattleParticipantSetBoundsEvent::invoker);
        });
    }

    public static Identifier createId(final String path) {
        return new Identifier(MOD_ID, path);
    }
}
