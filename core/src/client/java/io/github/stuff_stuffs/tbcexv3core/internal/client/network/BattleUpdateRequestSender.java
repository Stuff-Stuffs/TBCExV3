package io.github.stuff_stuffs.tbcexv3core.internal.client.network;

import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequest;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateRequestReceiver;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

import java.util.List;

public final class BattleUpdateRequestSender {
    private BattleUpdateRequestSender() {}

    public static void send(List<BattleUpdateRequest> updateRequests) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(updateRequests.size());
        for (BattleUpdateRequest updateRequest : updateRequests) {
            buf.encode(BattleUpdateRequest.CODEC, updateRequest);
        }
        ClientPlayNetworking.send(BattleUpdateRequestReceiver.CHANNEL, buf);
    }
}
