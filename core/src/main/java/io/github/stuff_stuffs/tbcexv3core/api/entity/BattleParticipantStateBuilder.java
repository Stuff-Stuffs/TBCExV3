package io.github.stuff_stuffs.tbcexv3core.api.entity;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.state.BattleParticipantState;
import io.github.stuff_stuffs.tbcexv3core.impl.entity.BattleParticipantStateBuilderImpl;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.UUID;

@ApiStatus.NonExtendable
public interface BattleParticipantStateBuilder {
    void addComponent(BattleEntityComponent component);

    Built build();

    static BattleParticipantStateBuilder create(final UUID uuid) {
        return new BattleParticipantStateBuilderImpl(uuid);
    }

    @ApiStatus.NonExtendable
    interface Built {
        UUID getUuid();

        List<BattleEntityComponent> getComponentList();

        BattleEntityComponentMap getComponents();

        void forEach(BattleParticipantState state);

        void forEach(BattleView view, ServerWorld world);

        void onJoin(Entity entity);

        static Codec<Built> codec(final boolean network) {
            return network ? BattleParticipantStateBuilderImpl.BuiltImpl.NETWORK_CODEC : BattleParticipantStateBuilderImpl.BuiltImpl.CODEC;
        }
    }
}
