package io.github.stuff_stuffs.tbcexv3core.impl.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.util.dynamic.Codecs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BattleParticipantStateBuilderImpl implements BattleParticipantStateBuilder {
    private final UUID uuid;
    private final Map<BattleEntityComponentType<?>, BattleEntityComponent> components = new Reference2ObjectLinkedOpenHashMap<>();

    public BattleParticipantStateBuilderImpl(final UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void addComponent(final BattleEntityComponent component) {
        final BattleEntityComponent current = components.put(component.getType(), component);
        if (current != null) {
            components.put(component.getType(), combine(component, current, component.getType()));
        }
    }

    @Override
    public Built build() {
        final BattleEntityComponent[] array = components.values().toArray(BattleEntityComponent[]::new);
        return new BuiltImpl(uuid, Arrays.asList(array));
    }

    private <T extends BattleEntityComponent> T combine(final BattleEntityComponent first, final BattleEntityComponent second, final BattleEntityComponentType<T> type) {
        final T firstCasted = type.checkedCast(first);
        final T secondCasted = type.checkedCast(second);
        if (firstCasted == null || secondCasted == null) {
            throw new TBCExException("Type mismatch");
        }
        return type.combine(firstCasted, secondCasted);
    }

    public record BuiltImpl(UUID uuid, List<BattleEntityComponent> components) implements Built {
        public static final Codec<Built> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.UUID.fieldOf("uuid").forGetter(Built::getUuid), Codec.list(BattleEntityComponent.CODEC).fieldOf("components").forGetter(Built::getComponents)).apply(instance, BuiltImpl::new));

        @Override
        public UUID getUuid() {
            return uuid;
        }

        @Override
        public List<BattleEntityComponent> getComponents() {
            return components;
        }

        @Override
        public void forEach(final BattleParticipantState state) {
            components.forEach(c -> c.applyToState(state));
        }
    }
}
