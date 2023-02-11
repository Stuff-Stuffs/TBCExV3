package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import com.mojang.serialization.Codec;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import java.util.List;

public final class BattleUpdateSender {
    public static final Identifier CHANNEL = TBCExV3Core.createId("battle_update");

    public static void send(List<BattleUpdate> updates, PacketSender sender, Registry<Biome> biomeRegistry) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(updates.size());
        final Codec<BattleUpdate> codec = BattleUpdate.codec(biomeRegistry);
        for (BattleUpdate update : updates) {
            buf.encode(codec, update);
        }
        sender.sendPacket(CHANNEL, buf);
    }

    private BattleUpdateSender() {
    }
}
