package io.github.stuff_stuffs.tbcexv3core.internal.client.network;

import io.github.stuff_stuffs.tbcexv3core.internal.common.network.PlayerCurrentBattleRequestReceiver;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public final class PlayerCurrentBattleRequestSender {
    private PlayerCurrentBattleRequestSender() {
    }

    public static void send() {
        ClientPlayNetworking.send(PlayerCurrentBattleRequestReceiver.IDENTIFIER, PacketByteBufs.create());
    }
}
