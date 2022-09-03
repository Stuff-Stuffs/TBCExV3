package io.github.stuff_stuffs.tbcexv3core.internal.client.world;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleView;
import io.github.stuff_stuffs.tbcexv3core.impl.ClientBattleImpl;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.client.network.BattleUpdateRequestSender;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClientBattleWorldContainer {
    private final Map<UUID, ClientBattleImpl> battles = new Object2ReferenceOpenHashMap<>();
    private final Set<UUID> battleUpdateRequestsToSend = new ObjectOpenHashSet<>();
    private final RegistryKey<World> worldKey;

    public ClientBattleWorldContainer(RegistryKey<World> worldKey) {
        this.worldKey = worldKey;
    }

    public @Nullable BattleView getBattle(UUID uuid) {
        battleUpdateRequestsToSend.add(uuid);
        return battles.get(uuid);
    }

    public void tick() {
        if(!battleUpdateRequestsToSend.isEmpty()) {
            List<BattleUpdateRequest> updateRequests = new ArrayList<>(battleUpdateRequestsToSend.size());
            for (UUID uuid : battleUpdateRequestsToSend) {
                if (battles.containsKey(uuid)) {
                    updateRequests.add(battles.get(uuid).createUpdateRequest());
                } else {
                    updateRequests.add(new BattleUpdateRequest(BattleHandle.of(worldKey, uuid), -1));
                }
            }
            BattleUpdateRequestSender.send(updateRequests);
        }
    }

    public void update(BattleUpdate update) {
        if(battles.containsKey(update.handle().getUuid())) {
            battles.get(update.handle().getUuid()).update(update);
        } else if(update.offset()==0) {
            ClientBattleImpl battle = new ClientBattleImpl(new BattleImpl());
            battle.update(update);
            battles.put(update.handle().getUuid(), battle);
        }
    }
}
