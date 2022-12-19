package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.ActionTrace;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.bounds.BattleParticipantBounds;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponent;
import io.github.stuff_stuffs.tbcexv3core.api.entity.component.BattleEntityComponentMap;
import io.github.stuff_stuffs.tbcexv3core.api.util.Tracer;
import io.github.stuff_stuffs.tbcexv3core.impl.entity.BattleParticipantStateBuilderImpl;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;

@ApiStatus.NonExtendable
public interface BattleParticipantStateBuilder {
    void addComponent(BattleEntityComponent component);

    Built build(Identifier team);

    static BattleParticipantStateBuilder create(final UUID uuid, final BattleParticipantBounds bounds) {
        return new BattleParticipantStateBuilderImpl(uuid, bounds);
    }

    @ApiStatus.NonExtendable
    interface Built {
        UUID getUuid();

        Identifier getTeam();

        List<BattleEntityComponent> getComponentList();

        BattleEntityComponentMap getComponents();

        BattleParticipantBounds getBounds();

        void forEach(BattleParticipantState state, Tracer<ActionTrace> tracer);

        void forEach(BattleView view, ServerWorld world);

        void onJoin(BattleHandle handle, Entity entity);

        static Codec<Built> codec(final boolean network) {
            return network ? BattleParticipantStateBuilderImpl.BuiltImpl.NETWORK_CODEC : BattleParticipantStateBuilderImpl.BuiltImpl.CODEC;
        }
    }
}
