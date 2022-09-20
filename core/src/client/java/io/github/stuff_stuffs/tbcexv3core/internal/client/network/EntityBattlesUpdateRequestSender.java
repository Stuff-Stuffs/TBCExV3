package io.github.stuff_stuffs.tbcexv3core.internal.client.network;

import io.github.stuff_stuffs.tbcexv3core.internal.common.network.EntityBattlesUpdateRequestReceiver;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

import java.util.Set;
import java.util.UUID;

public final class EntityBattlesUpdateRequestSender {
    public static void send(final Set<UUID> updateRequests) {
        final PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(updateRequests.size());
        for (final UUID updateRequest : updateRequests) {
            buf.writeUuid(updateRequest);
        }
        ClientPlayNetworking.send(EntityBattlesUpdateRequestReceiver.CHANNEL, buf);
    }

    private EntityBattlesUpdateRequestSender() {
    }
}
