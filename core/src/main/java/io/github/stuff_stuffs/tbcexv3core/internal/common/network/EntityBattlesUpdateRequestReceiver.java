package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//TODO read from other worlds
public final class EntityBattlesUpdateRequestReceiver {
    public static final Identifier CHANNEL = TBCExV3Core.createId("entity_battles_update_request");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, EntityBattlesUpdateRequestReceiver::receive);
    }

    private static void receive(final MinecraftServer server, final ServerPlayerEntity entity, final ServerPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        final int count = buf.readVarInt();
        final List<UUID> requests = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            requests.add(buf.readUuid());
        }
        server.execute(() -> {
            for (final UUID request : requests) {
                final ServerWorld world = entity.getWorld();
                if (world != null) {
                    final List<BattleParticipantHandle> activeBattles = ((ServerBattleWorld) world).getBattles(request, TriState.TRUE);
                    final List<BattleParticipantHandle> inactiveBattles = ((ServerBattleWorld) world).getBattles(request, TriState.TRUE);
                    EntityBattlesUpdateSender.send(entity, request, activeBattles, inactiveBattles);
                }
            }
        });
    }

    private EntityBattlesUpdateRequestReceiver() {
    }
}
