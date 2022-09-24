package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PlayerCurrentBattleSender {
    public static final Identifier CHANNEL = TBCExV3Core.createId("player_current_battle");

    public static void send(final ServerPlayerEntity entity, @Nullable final BattleHandle handle) {
        final PacketByteBuf buf = PacketByteBufs.create();
        if (handle == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeIdentifier(handle.getWorldKey().getValue());
            buf.writeUuid(handle.getUuid());
        }
        ServerPlayNetworking.send(entity, CHANNEL, buf);
    }

    private PlayerCurrentBattleSender() {
    }
}
