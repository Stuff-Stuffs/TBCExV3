package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.List;

public final class BattleUpdateSender {
    public static final Identifier CHANNEL = TBCExV3Core.createId("battle_update");

    public static void send(List<BattleUpdate> updates, PacketSender sender) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(updates.size());
        for (BattleUpdate update : updates) {
            buf.encode(BattleUpdate.CODEC, update);
        }
        sender.sendPacket(CHANNEL, buf);
    }

    private BattleUpdateSender() {
    }
}
