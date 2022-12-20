package io.github.stuff_stuffs.tbcexv3core.internal.client.world;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.impl.ClientBattleImpl;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.EntityBattlesUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class ClientBattleWorldContainer {
    private final Map<UUID, ClientBattleImpl> battles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<UUID>> entityBattles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<UUID>> activeEntityBattles = new Object2ReferenceOpenHashMap<>();
    private final Set<UUID> battleUpdateRequestsToSend = new ObjectOpenHashSet<>();
    private final Set<UUID> entityBattleRequestsToSend = new ObjectOpenHashSet<>();
    private final RegistryKey<World> worldKey;

    public ClientBattleWorldContainer(final RegistryKey<World> worldKey) {
        this.worldKey = worldKey;
    }

    public @Nullable BattleView getBattle(final UUID uuid) {
        battleUpdateRequestsToSend.add(uuid);
        return battles.get(uuid);
    }

    public void tick() {
        if (!battleUpdateRequestsToSend.isEmpty()) {
            final List<BattleUpdateRequest> updateRequests = new ArrayList<>(battleUpdateRequestsToSend.size());
            for (final UUID uuid : battleUpdateRequestsToSend) {
                if (battles.containsKey(uuid)) {
                    updateRequests.add(battles.get(uuid).createUpdateRequest());
                } else {
                    updateRequests.add(new BattleUpdateRequest(BattleHandle.of(worldKey, uuid), -1));
                }
            }
            BattleUpdateRequestSender.send(updateRequests);
            battleUpdateRequestsToSend.clear();
        }
        if (!entityBattleRequestsToSend.isEmpty()) {
            EntityBattlesUpdateRequestSender.send(entityBattleRequestsToSend);
            entityBattleRequestsToSend.clear();
        }
    }

    public void update(final BattleUpdate update) {
        if (battles.containsKey(update.handle().getUuid())) {
            battles.get(update.handle().getUuid()).update(update);
        } else if (update.offset() == 0) {
            final ClientBattleImpl battle = new ClientBattleImpl(new BattleImpl(update.handle(), BattleStateMode.CLIENT));
            battle.update(update);
            battles.put(update.handle().getUuid(), battle);
        }
    }

    public void update(final UUID entityId, final List<UUID> battles, final List<UUID> inactiveBattles) {
        activeEntityBattles.put(entityId, battles);
        entityBattles.put(entityId, inactiveBattles);
    }

    public List<BattleParticipantHandle> getEntityBattles(final UUID entityId, final TriState active) {
        entityBattleRequestsToSend.add(entityId);
        if (active == TriState.TRUE) {
            return activeEntityBattles.getOrDefault(entityId, List.of()).stream().map(id -> BattleParticipantHandle.of(entityId, BattleHandle.of(worldKey, id))).toList();
        } else if (active == TriState.FALSE) {
            return entityBattles.getOrDefault(entityId, List.of()).stream().map(id -> BattleParticipantHandle.of(entityId, BattleHandle.of(worldKey, id))).toList();
        } else {
            return Stream.concat(activeEntityBattles.getOrDefault(entityId, List.of()).stream(), entityBattles.getOrDefault(entityId, List.of()).stream()).map(id -> BattleParticipantHandle.of(entityId, BattleHandle.of(worldKey, id))).toList();
        }
    }
}
