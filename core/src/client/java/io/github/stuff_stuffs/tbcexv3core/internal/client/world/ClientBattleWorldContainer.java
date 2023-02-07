package io.github.stuff_stuffs.tbcexv3core.internal.client.world;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.state.BattleStateMode;
import io.github.stuff_stuffs.tbcexv3core.impl.ClientBattleImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.EntityBattlesUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.util.TriState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class ClientBattleWorldContainer {
    private final Map<BattleHandle, ClientBattleImpl> battles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<BattleParticipantHandle>> entityBattles = new Object2ReferenceOpenHashMap<>();
    private final Map<UUID, List<BattleParticipantHandle>> activeEntityBattles = new Object2ReferenceOpenHashMap<>();
    private final Set<BattleHandle> battleUpdateRequestsToSend = new ObjectOpenHashSet<>();
    private final Set<UUID> entityBattleRequestsToSend = new ObjectOpenHashSet<>();

    public ClientBattleWorldContainer() {
    }

    public @Nullable BattleView getBattle(final BattleHandle handle) {
        battleUpdateRequestsToSend.add(handle);
        return battles.get(handle);
    }

    public void tick() {
        if (!battleUpdateRequestsToSend.isEmpty()) {
            final List<BattleUpdateRequest> updateRequests = new ArrayList<>(battleUpdateRequestsToSend.size());
            for (final BattleHandle handle : battleUpdateRequestsToSend) {
                if (battles.containsKey(handle)) {
                    updateRequests.add(battles.get(handle).createUpdateRequest());
                } else {
                    updateRequests.add(new BattleUpdateRequest(handle, -1));
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
        if (battles.containsKey(update.handle())) {
            battles.get(update.handle()).update(update);
        } else if (update.offset() == 0 && update.initialData().isPresent()) {
            final BattleUpdate.InitialData data = update.initialData().get();
            final ClientBattleImpl battle = new ClientBattleImpl(update.handle(), BattleStateMode.CLIENT, data.initialEnvironment(), data.origin());
            battle.update(update);
            battles.put(update.handle(), battle);
        }
    }

    public void update(final UUID entityId, final List<BattleParticipantHandle> battles, final List<BattleParticipantHandle> inactiveBattles) {
        activeEntityBattles.put(entityId, battles);
        entityBattles.put(entityId, inactiveBattles);
    }

    public List<BattleParticipantHandle> getEntityBattles(final UUID entityId, final TriState active) {
        entityBattleRequestsToSend.add(entityId);
        if (active == TriState.TRUE) {
            return activeEntityBattles.getOrDefault(entityId, List.of());
        } else if (active == TriState.FALSE) {
            return entityBattles.getOrDefault(entityId, List.of());
        } else {
            return Stream.concat(activeEntityBattles.getOrDefault(entityId, List.of()).stream(), entityBattles.getOrDefault(entityId, List.of()).stream()).toList();
        }
    }
}
