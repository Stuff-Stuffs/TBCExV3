package io.github.stuff_stuffs.tbcexv3core.internal.client.network;

import io.github.stuff_stuffs.tbcexv3core.internal.client.world.ClientBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.EntityBattlesUpdateSender;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class EntityBattlesUpdateReceiver {
    private EntityBattlesUpdateReceiver() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(EntityBattlesUpdateSender.CHANNEL, EntityBattlesUpdateReceiver::receive);
    }

    private static void receive(final MinecraftClient client, final ClientPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        final UUID entityId = buf.readUuid();
        final int count = buf.readVarInt();
        final List<UUID> activeBattles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            activeBattles.add(buf.readUuid());
        }
        final int inactiveCount = buf.readVarInt();
        final List<UUID> inactiveBattles = new ArrayList<>(inactiveCount);
        for (int i = 0; i < inactiveCount; i++) {
            inactiveBattles.add(buf.readUuid());
        }
        client.execute(() -> {
            if (client.world != null) {
                ((ClientBattleWorld) client.world).update(entityId, activeBattles, inactiveBattles);
            }
        });
    }
}
