package io.github.stuff_stuffs.tbcexv3core.api.entity.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.trace.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventory;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.BattleParticipantInventoryHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.equipment.BattleParticipantEquipmentSlot;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.inventory.item.BattleParticipantItemStack;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BinaryOperator;

public class InventoryBattleEntityComponent implements BattleEntityComponent {
    public static final Codec<InventoryBattleEntityComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(BattleParticipantItemStack.codec()).fieldOf("stacks").forGetter(component -> component.stacks),
            Codec.unboundedMap(BattleParticipantEquipmentSlot.CODEC, BattleParticipantItemStack.codec()).fieldOf("equipment").forGetter(component -> component.equipment)
    ).apply(instance, InventoryBattleEntityComponent::new));
    public static final BinaryOperator<InventoryBattleEntityComponent> COMBINER = (first, second) -> {
        final List<BattleParticipantItemStack> combinedStacks = new ArrayList<>(first.stacks.size() + second.stacks.size());
        combinedStacks.addAll(first.stacks);
        combinedStacks.addAll(second.stacks);
        final Map<BattleParticipantEquipmentSlot, BattleParticipantItemStack> combinedEquipment = new Object2ReferenceOpenHashMap<>(first.equipment);
        for (final Map.Entry<BattleParticipantEquipmentSlot, BattleParticipantItemStack> entry : second.equipment.entrySet()) {
            if (combinedEquipment.put(entry.getKey(), entry.getValue()) != null) {
                throw new TBCExException("Could not combine Inventory component! Duplicate equipment detected!");
            }
        }
        return new InventoryBattleEntityComponent(combinedStacks, combinedEquipment);
    };
    private final List<BattleParticipantItemStack> stacks;
    private final Map<BattleParticipantEquipmentSlot, BattleParticipantItemStack> equipment;

    private InventoryBattleEntityComponent(final List<BattleParticipantItemStack> stacks, final Map<BattleParticipantEquipmentSlot, BattleParticipantItemStack> equipment) {
        this.stacks = stacks;
        this.equipment = equipment;
    }

    @Override
    public void applyToState(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {
        final BattleParticipantInventory inventory = state.getInventory();
        for (final BattleParticipantItemStack stack : stacks) {
            if (inventory.give(stack, tracer).isEmpty()) {
                throw new TBCExException("Could not give setup item to participant!");
            }
        }
        for (final Map.Entry<BattleParticipantEquipmentSlot, BattleParticipantItemStack> entry : equipment.entrySet()) {
            final Optional<BattleParticipantInventoryHandle> handle = inventory.give(entry.getValue(), tracer);
            if (handle.isEmpty()) {
                throw new TBCExException("Could not give setup item to participant!");
            }
            if (!inventory.equip(entry.getKey(), handle.get(), false, tracer)) {
                throw new TBCExException("Could not equip setup equipment to participant!");
            }
        }
    }

    @Override
    public void onLeave(final BattleView view, final ServerWorld world) {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<BattleParticipantItemStack> stacks = new ArrayList<>();
        private final Map<BattleParticipantEquipmentSlot, BattleParticipantItemStack> equipment = new Object2ReferenceOpenHashMap<>();

        private Builder() {
        }

        public Builder addStack(final BattleParticipantItemStack stack) {
            stacks.add(stack);
            return this;
        }

        public Builder setEquipment(final BattleParticipantEquipmentSlot slot, final BattleParticipantItemStack stack) {
            equipment.put(slot, stack);
            return this;
        }

        public Optional<BattleParticipantItemStack> getEquipment(final BattleParticipantEquipmentSlot slot) {
            return Optional.ofNullable(equipment.getOrDefault(slot, null));
        }

        public InventoryBattleEntityComponent build() {
            return new InventoryBattleEntityComponent(new ArrayList<>(stacks), new Object2ReferenceOpenHashMap<>(equipment));
        }
    }

    @Override
    public BattleEntityComponentType<?> getType() {
        return CoreBattleEntityComponents.INVENTORY_BATTLE_ENTITY_COMPONENT_TYPE;
    }
}
