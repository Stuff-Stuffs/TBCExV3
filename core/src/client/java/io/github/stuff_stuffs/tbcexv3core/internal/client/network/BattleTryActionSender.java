package io.github.stuff_stuffs.tbcexv3core.internal.client.network;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleTryActionReceiver;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public final class BattleTryActionSender {
    private BattleTryActionSender() {
    }

    public static void send(final BattleHandle handle, final BattleAction action) {
        final PacketByteBuf buf = PacketByteBufs.create();
        buf.encode(BattleHandle.codec(), handle);
        buf.encode(BattleAction.CODEC, action);
        ClientPlayNetworking.send(BattleTryActionReceiver.IDENTIFIER, buf);
    }
}
