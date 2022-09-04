package io.github.stuff_stuffs.tbcexv3core.impl.entity;

import com.mojang.datafixers.util.Pair;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffect;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.effect.BattleParticipantEffectType;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class BattleParticipantStateBuilderImpl implements BattleParticipantStateBuilder {
    private final Map<BattleParticipantEffectType<?,?>, BattleParticipantEffect> effects;
    private final List<Pair<BattleParticipantItemStack, OptionalInt>> items;
    private final Map<BattleParticipantEquipmentSlot, BattleParticipantItemStack> equipment;

    public BattleParticipantStateBuilderImpl() {
        effects = new Reference2ObjectOpenHashMap<>();
        items = new ArrayList<>();
        equipment = new Reference2ObjectOpenHashMap<>();
    }

    @Override
    public void addEffect(BattleParticipantEffect effect) {
        final BattleParticipantEffectType<?, ?> type = effect.getType();
        final BattleParticipantEffect current = effects.getOrDefault(type, null);
        if(current==null) {
            effects.put(type, effect);
        } else {
            if(current.getType()!= type) {
                throw new IllegalStateException();
            }
            final BattleParticipantEffect combined = combine(effect, current, type);
            if(combined.getType()!= type) {
                throw new IllegalStateException();
            }
            effects.put(type, combined);
        }
    }

    private <View extends BattleParticipantEffect, Effect extends View, Type extends BattleParticipantEffectType<View, Effect>> Effect combine(BattleParticipantEffect first, BattleParticipantEffect second, Type type) {
        final Effect firstCasted = type.checkedCast(first);
        final Effect secondCasted = type.checkedCast(second);
        if(firstCasted==null||secondCasted==null) {
            throw new IllegalStateException();
        }
        return type.combine(firstCasted, secondCasted);
    }

    @Override
    public void addItem(BattleParticipantItemStack stack, OptionalInt slot) {
        items.add(Pair.of(stack, slot));
    }

    @Override
    public boolean tryEquip(BattleParticipantItemStack stack, BattleParticipantEquipmentSlot slot) {
        for (BattleParticipantEquipmentSlot equipmentSlot : equipment.keySet()) {
            if(equipmentSlot.getReference().isIn(slot.getBlocks()) || slot.getReference().isIn(equipmentSlot.getBlocks())
            || equipmentSlot.getReference().isIn(slot.getBlockedBy()) || slot.getReference().isIn(equipmentSlot.getBlockedBy())) {
                return false;
            }
        }
        BattleParticipantItemStack prev;
        if((prev=equipment.put(slot, stack))!=null) {
            addItem(prev, OptionalInt.empty());
        }
        return true;
    }
}
