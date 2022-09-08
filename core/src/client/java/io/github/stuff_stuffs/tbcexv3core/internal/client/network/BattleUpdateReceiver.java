package io.github.stuff_stuffs.tbcexv3core.internal.client.network;

import io.github.stuff_stuffs.tbcexv3core.internal.client.world.ClientBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdate;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.BattleUpdateSender;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;

public final class BattleUpdateReceiver {
    private BattleUpdateReceiver() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(BattleUpdateSender.CHANNEL, BattleUpdateReceiver::receive);
    }

    private static void receive(final MinecraftClient client, final ClientPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        final int count = buf.readVarInt();
        final List<BattleUpdate> updates = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            updates.add(buf.decode(BattleUpdate.CODEC));
        }
        client.execute(() -> {
            for (final BattleUpdate update : updates) {
                if (client.world != null && client.world.getRegistryKey().equals(update.handle().getWorldKey())) {
                    ((ClientBattleWorld) client.world).update(update);
                }
            }
        });
    }
}
