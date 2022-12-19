package io.github.stuff_stuffs.tbcexv3core.impl.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.BattleParticipantStateBuilder;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentMap;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentType;
import io.github.stuff_stuffs.tbcexv3core.api.util.TBCExException;
import io.github.stuff_stuffs.tbcexv3core.api.util.TopologicalSort;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BattleParticipantStateBuilderImpl implements BattleParticipantStateBuilder {
    private final UUID uuid;
    private final BattleParticipantBounds bounds;
    private final Map<BattleEntityComponentType<?>, BattleEntityComponent> components = new Reference2ObjectLinkedOpenHashMap<>();

    public BattleParticipantStateBuilderImpl(final UUID uuid, final BattleParticipantBounds bounds) {
        this.uuid = uuid;
        this.bounds = bounds;
    }

    @Override
    public void addComponent(final BattleEntityComponent component) {
        final BattleEntityComponent current = components.put(component.getType(), component);
        if (current != null) {
            components.put(component.getType(), combine(component, current, component.getType()));
        }
    }

    @Override
    public Built build(final Identifier team) {
        final BattleEntityComponent[] array = components.values().toArray(BattleEntityComponent[]::new);
        return new BuiltImpl(uuid, Arrays.asList(array), bounds, team);
    }

    private <T extends BattleEntityComponent> T combine(final BattleEntityComponent first, final BattleEntityComponent second, final BattleEntityComponentType<T> type) {
        final T firstCasted = type.checkedCast(first);
        final T secondCasted = type.checkedCast(second);
        if (firstCasted == null || secondCasted == null) {
            throw new TBCExException("Type mismatch");
        }
        return type.combine(firstCasted, secondCasted);
    }

    public record BuiltImpl(
            UUID uuid,
            List<BattleEntityComponent> components,
            BattleParticipantBounds bounds,
            Identifier team
    ) implements Built {
        public static final Codec<Built> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.UUID.fieldOf("uuid").forGetter(Built::getUuid), Codec.list(BattleEntityComponent.CODEC).fieldOf("components").forGetter(Built::getComponentList), BattleParticipantBounds.CODEC.fieldOf("bounds").forGetter(Built::getBounds), Identifier.CODEC.fieldOf("team").forGetter(Built::getTeam)).apply(instance, BuiltImpl::new));
        public static final Codec<Built> NETWORK_CODEC = RecordCodecBuilder.create(instance -> instance.group(Codecs.UUID.fieldOf("uuid").forGetter(Built::getUuid), Codec.list(BattleEntityComponent.NETWORK_CODEC).fieldOf("components").forGetter(Built::getComponentList), BattleParticipantBounds.CODEC.fieldOf("bounds").forGetter(Built::getBounds), Identifier.CODEC.fieldOf("team").forGetter(Built::getTeam)).apply(instance, BuiltImpl::new));

        public BuiltImpl(final UUID uuid, final List<BattleEntityComponent> components, final BattleParticipantBounds bounds, final Identifier team) {
            this.uuid = uuid;
            this.components = TopologicalSort.sort(components, (parent, child, items) -> {
                final BattleEntityComponent parentComponent = items.get(parent);
                final Identifier parentId = BattleEntityComponentType.REGISTRY.getId(parentComponent.getType());
                final BattleEntityComponent childComponent = items.get(parent);
                final Identifier childId = BattleEntityComponentType.REGISTRY.getId(childComponent.getType());
                return childComponent.getType().happensAfter().contains(parentId) || parentComponent.getType().happensBefore().contains(childId);
            });
            this.bounds = bounds;
            this.team = team;
        }

        @Override
        public UUID getUuid() {
            return uuid;
        }

        @Override
        public Identifier getTeam() {
            return team;
        }

        @Override
        public BattleEntityComponentMap getComponents() {
            final BattleEntityComponentMap.Builder builder = BattleEntityComponentMap.builder();
            for (final BattleEntityComponent component : components) {
                addComponentToMap(component.getType(), component, builder);
            }
            return builder.build();
        }

        @Override
        public BattleParticipantBounds getBounds() {
            return null;
        }

        @Override
        public List<BattleEntityComponent> getComponentList() {
            return components;
        }

        private static <T extends BattleEntityComponent> void addComponentToMap(final BattleEntityComponentType<T> type, final BattleEntityComponent component, final BattleEntityComponentMap.Builder builder) {
            builder.add(type, (T) component);
        }

        @Override
        public void forEach(final BattleParticipantState state, final Tracer<ActionTrace> tracer) {
            components.forEach(c -> c.applyToState(state, tracer));
        }

        @Override
        public void forEach(final BattleView view, final ServerWorld world) {
            for (final BattleEntityComponent component : components) {
                component.onLeave(view, world);
            }
        }

        @Override
        public void onJoin(final BattleHandle handle, final Entity entity) {
            for (final BattleEntityComponent component : components) {
                component.applyToEntityOnJoin(BattleParticipantHandle.of(entity.getUuid(), handle), entity);
            }
        }
    }
}
