package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class PlayerCurrentBattleRequestReceiver {
    public static final Identifier IDENTIFIER = TBCExV3Core.createId("player_current_battle_request");

    public static void init(final ServerPlayNetworkHandler handler) {
        ServerPlayNetworking.registerReceiver(handler, IDENTIFIER, PlayerCurrentBattleRequestReceiver::receive);
    }

    private static void receive(final MinecraftServer server, final ServerPlayerEntity entity, final ServerPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        server.execute(() -> PlayerCurrentBattleSender.send(entity, ((TBCExPlayerEntity) entity).tbcex$getCurrentBattle()));
    }

    private PlayerCurrentBattleRequestReceiver() {
    }
}
