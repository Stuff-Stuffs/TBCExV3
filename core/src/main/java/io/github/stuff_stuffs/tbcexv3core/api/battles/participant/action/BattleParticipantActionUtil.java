package io.github.stuff_stuffs.tbcexv3core.api.battles.participant.action;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantStateView;

import java.util.*;

public final class BattleParticipantActionUtil {
    public static List<Pair<BattleParticipantActionSource, BattleParticipantAction>> gather(final BattleParticipantStateView state) {
        final BattleParticipantInventoryView inventory = state.getInventory();
        final Iterator<BattleParticipantInventoryHandle> handles = inventory.getHandles();
        final List<Pair<BattleParticipantActionSource, BattleParticipantAction>> list = new ArrayList<>();
        while (handles.hasNext()) {
            final BattleParticipantInventoryHandle next = handles.next();
            final Optional<BattleParticipantItemStack> stack = inventory.getStack(next);
            if (stack.isPresent()) {
                final Optional<BattleParticipantEquipmentSlot> slot = inventory.getSlot(next);
                final List<BattleParticipantAction> actions = stack.get().getItem().actions(state, stack.get(), Optional.of(next));
                final BattleParticipantActionSource source = slot.isPresent() ? new BattleParticipantActionSource.Equipped(slot.get()) : new BattleParticipantActionSource.Item(next);
                for (final BattleParticipantAction action : actions) {
                    if (BattleParticipantActionAppendEvent.EVENT.invoker().accept(state, source, action)) {
                        list.add(Pair.of(source, action));
                    }
                }
            }
        }
        final Iterator<BattleParticipantEffectType<?, ?>> effects = state.getEffects();
        while (effects.hasNext()) {
            final BattleParticipantEffectType<?, ?> type = effects.next();
            final Optional<? extends BattleParticipantEffect> view = state.getEffectView(type);
            if (view.isPresent()) {
                final BattleParticipantActionSource source = new BattleParticipantActionSource.Effect(type);
                for (final BattleParticipantAction action : view.get().getActions()) {
                    if (BattleParticipantActionAppendEvent.EVENT.invoker().accept(state, source, action)) {
                        list.add(Pair.of(source, action));
                    }
                }
            }
        }
        BattleParticipantDefaultActionGatherEvent.EVENT.invoker().gather(state, action -> {
            final BattleParticipantActionSource source = new BattleParticipantActionSource.Default();
            if (BattleParticipantActionAppendEvent.EVENT.invoker().accept(state, source, action)) {
                list.add(Pair.of(source, action));
            }
        });
        return list;
    }

    private BattleParticipantActionUtil() {
    }
}
