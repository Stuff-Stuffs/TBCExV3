package io.github.stuff_stuffs.tbcexv3core.impl.battle.participant.inventory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.event.CoreBattleParticipantEvents;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.event.EventMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryEntryList;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class BattleParticipantInventoryImpl implements AbstractBattleParticipantInventory {
    public static final Codec<BattleParticipantInventoryImpl> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.LONG, BattleParticipantItemStack.codec()).fieldOf("stacks").forGetter(inventory -> inventory.stacks),
            Codec.simpleMap(BattleParticipantEquipmentSlot.CODEC, BattleParticipantInventoryHandle.codec(), BattleParticipantEquipmentSlot.REGISTRY).fieldOf("equipped").forGetter(inventory -> inventory.equipped)
    ).apply(instance, BattleParticipantInventoryImpl::new));
    private final Long2ReferenceMap<BattleParticipantItemStack> stacks;
    private final BiMap<BattleParticipantEquipmentSlot, BattleParticipantInventoryHandle> equipped;
    private BattleParticipantHandle handle;
    private BattleParticipantState state;
    private long nextKey;

    private BattleParticipantInventoryImpl(final Map<Long, BattleParticipantItemStack> stacks, final Map<BattleParticipantEquipmentSlot, BattleParticipantInventoryHandle> equipped) {
        this.stacks = new Long2ReferenceOpenHashMap<>();
        long max = 0;
        for (final Map.Entry<Long, BattleParticipantItemStack> entry : stacks.entrySet()) {
            this.stacks.put((long) entry.getKey(), entry.getValue());
            max = Math.max(max, entry.getKey());
        }
        this.equipped = HashBiMap.create(equipped);
        nextKey = max + 1;
    }

    public BattleParticipantInventoryImpl() {
        stacks = new Long2ReferenceOpenHashMap<>();
        equipped = HashBiMap.create();
    }

    @Override
    public Optional<BattleParticipantItemStack> getStack(final BattleParticipantInventoryHandle handle) {
        checkSetup();
        if (this.handle.equals(handle.getParentHandle())) {
            throw new TBCExException();
        }
        if (!(handle instanceof AbstractBattleParticipantInventoryHandle abstractHandle)) {
            throw new TBCExException();
        }
        return Optional.ofNullable(stacks.getOrDefault(abstractHandle.getKey(), null));
    }

    @Override
    public Optional<BattleParticipantInventoryHandle> getHandle(final BattleParticipantEquipmentSlot slot) {
        checkSetup();
        if (!(handle instanceof AbstractBattleParticipantInventoryHandle)) {
            throw new TBCExException();
        }
        return Optional.ofNullable(equipped.getOrDefault(slot, null));
    }

    @Override
    public Optional<BattleParticipantEquipmentSlot> getSlot(final BattleParticipantInventoryHandle handle) {
        checkSetup();
        if (this.handle.equals(handle.getParentHandle())) {
            throw new TBCExException();
        }
        if (!(handle instanceof AbstractBattleParticipantInventoryHandle)) {
            throw new TBCExException();
        }
        return Optional.ofNullable(equipped.inverse().getOrDefault(handle, null));
    }

    @Override
    public Iterator<BattleParticipantInventoryHandle> getHandles() {
        checkSetup();
        final LongSet keys = new LongOpenHashSet(stacks.keySet());
        return keys.longStream().<BattleParticipantInventoryHandle>mapToObj(l -> new BattleParticipantInventoryHandleImpl(handle, l)).iterator();
    }

    @Override
    public boolean swapStack(final BattleParticipantInventoryHandle handle, final BattleParticipantItemStack stack) {
        checkSetup();
        if (this.handle.equals(handle.getParentHandle())) {
            throw new TBCExException();
        }
        if (!(handle instanceof AbstractBattleParticipantInventoryHandle abstractHandle)) {
            throw new TBCExException();
        }
        final EventMap eventMap = state.getEventMap();
        if (stacks.containsKey(abstractHandle.getKey()) && eventMap.getEvent(CoreBattleParticipantEvents.PRE_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().preTakeItem(handle, stacks.get(abstractHandle.getKey()), state)) {
            if (eventMap.getEvent(CoreBattleParticipantEvents.PRE_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().preGiveItem(stack, state)) {
                final BattleParticipantItemStack currentStack = stacks.put(abstractHandle.getKey(), stack);
                eventMap.getEvent(CoreBattleParticipantEvents.POST_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().postTakeItem(currentStack, state);
                eventMap.getEvent(CoreBattleParticipantEvents.POST_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().postGiveItem(handle, stack, state);
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<BattleParticipantInventoryHandle> give(final BattleParticipantItemStack stack) {
        checkSetup();
        final EventMap eventMap = state.getEventMap();
        if (eventMap.getEvent(CoreBattleParticipantEvents.PRE_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().preGiveItem(stack, state)) {
            long next;
            do {
                next = nextKey++;
            } while (stacks.containsKey(next));
            final BattleParticipantInventoryHandle handle = AbstractBattleParticipantInventoryHandle.of(this.handle, next);
            stacks.put(nextKey, stack);
            eventMap.getEvent(CoreBattleParticipantEvents.POST_GIVE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().postGiveItem(handle, stack, state);
            return Optional.of(handle);
        }
        return Optional.empty();
    }

    @Override
    public Optional<BattleParticipantItemStack> takeStack(final BattleParticipantInventoryHandle handle) {
        checkSetup();
        if (this.handle.equals(handle.getParentHandle())) {
            throw new TBCExException();
        }
        if (!(handle instanceof AbstractBattleParticipantInventoryHandle abstractHandle)) {
            throw new TBCExException();
        }
        final EventMap eventMap = state.getEventMap();
        if (stacks.containsKey(abstractHandle.getKey())) {
            if (equipped.inverse().containsKey(handle)) {
                final BattleParticipantEquipmentSlot slot = equipped.inverse().get(handle);
                if (eventMap.getEvent(CoreBattleParticipantEvents.PRE_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().preUnequip(state, handle, slot) && eventMap.getEvent(CoreBattleParticipantEvents.PRE_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().preTakeItem(handle, stacks.get(abstractHandle.getKey()), state)) {
                    equipped.remove(slot);
                    final BattleParticipantItemStack stack = stacks.remove(abstractHandle.getKey());
                    eventMap.getEvent(CoreBattleParticipantEvents.POST_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().postUnequip(state, handle, slot, stack);
                    eventMap.getEvent(CoreBattleParticipantEvents.POST_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().postTakeItem(stack, state);
                    return Optional.of(stack);
                }
            } else if (eventMap.getEvent(CoreBattleParticipantEvents.PRE_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().preTakeItem(handle, stacks.get(abstractHandle.getKey()), state)) {
                final BattleParticipantItemStack stack = stacks.remove(abstractHandle.getKey());
                eventMap.getEvent(CoreBattleParticipantEvents.POST_TAKE_BATTLE_PARTICIPANT_ITEM_EVENT).getInvoker().postTakeItem(stack, state);
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equip(final BattleParticipantEquipmentSlot slot, final BattleParticipantInventoryHandle handle, final boolean swap) {
        checkSetup();
        if (this.handle.equals(handle.getParentHandle())) {
            throw new TBCExException();
        }
        if (!checkSlot(slot)) {
            return false;
        }
        if (!(handle instanceof AbstractBattleParticipantInventoryHandle abstractHandle)) {
            throw new TBCExException();
        }
        final BattleParticipantItemStack stack = stacks.getOrDefault(abstractHandle.getKey(), null);
        if (stack == null) {
            return false;
        }
        final Optional<TagKey<BattleParticipantEquipmentSlot>> acceptableSlots = stack.getItem().getType().getAcceptableSlots();
        if (acceptableSlots.isEmpty() || !slot.getReference().isIn(acceptableSlots.get())) {
            return false;
        }
        if (equipped.inverse().getOrDefault(handle, null) != null) {
            return false;
        }
        final BattleParticipantInventoryHandle equippedHandle = equipped.getOrDefault(slot, null);
        final EventMap eventMap = state.getEventMap();
        if (equippedHandle == null) {
            if (eventMap.getEvent(CoreBattleParticipantEvents.PRE_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().preEquip(state, handle, slot)) {
                equipped.put(slot, handle);
                eventMap.getEvent(CoreBattleParticipantEvents.POST_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().postEquip(state, handle, slot);
                return true;
            }
        } else if (swap) {
            if (eventMap.getEvent(CoreBattleParticipantEvents.PRE_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().preUnequip(state, equippedHandle, slot)
                    && eventMap.getEvent(CoreBattleParticipantEvents.PRE_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().preEquip(state, handle, slot)) {
                equipped.put(slot, handle);
                eventMap.getEvent(CoreBattleParticipantEvents.POST_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().postUnequip(state, equippedHandle, slot, stack);
                eventMap.getEvent(CoreBattleParticipantEvents.POST_EQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().postEquip(state, handle, slot);
                return true;
            }
        }
        return false;
    }

    private boolean checkSlot(final BattleParticipantEquipmentSlot slot) {
        final RegistryEntryList<BattleParticipantEquipmentSlot> blockedByList = BattleParticipantEquipmentSlot.REGISTRY.getOrCreateEntryList(slot.getBlockedBy());
        final RegistryEntryList<BattleParticipantEquipmentSlot> blocksList = BattleParticipantEquipmentSlot.REGISTRY.getOrCreateEntryList(slot.getBlocks());
        for (final BattleParticipantEquipmentSlot equippedSlot : equipped.keySet()) {
            final RegistryEntry.Reference<BattleParticipantEquipmentSlot> reference = equippedSlot.getReference();
            if (blockedByList.contains(reference) || blocksList.contains(reference)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean unequip(final BattleParticipantEquipmentSlot slot) {
        checkSetup();
        final BattleParticipantInventoryHandle handle = equipped.getOrDefault(slot, null);
        if (handle == null) {
            return false;
        }
        final EventMap eventMap = state.getEventMap();
        if (eventMap.getEvent(CoreBattleParticipantEvents.PRE_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().preUnequip(state, handle, slot)) {
            equipped.remove(slot);
            final BattleParticipantItemStack stack = stacks.get(((AbstractBattleParticipantInventoryHandle) handle).getKey());
            eventMap.getEvent(CoreBattleParticipantEvents.POST_UNEQUIP_BATTLE_PARTICIPANT_EQUIPMENT_EVENT).getInvoker().postUnequip(state, handle, slot, stack);
            return true;
        }
        return false;
    }

    @Override
    public void setup(final BattleParticipantState state, final BattleParticipantHandle handle) {
        this.state = state;
        this.handle = handle;
    }

    private void checkSetup() {
        if (handle == null || state == null) {
            throw new TBCExException();
        }
    }
}
