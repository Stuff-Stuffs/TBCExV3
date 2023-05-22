package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import io.github.stuff_stuffs.tbcexv3core.api.battles.participant.BattleParticipantHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

public final class EntityBattlesUpdateSender {
    public static final Identifier CHANNEL = TBCExV3Core.createId("entity_battles_update");

    public static void send(final ServerPlayerEntity entity, final UUID entityId, final List<BattleParticipantHandle> activeBattles, final List<BattleParticipantHandle> inactiveBattles) {
        final PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(entityId);
        buf.writeVarInt(activeBattles.size());
        for (final BattleParticipantHandle id : activeBattles) {
            buf.encode(NbtOps.INSTANCE, BattleParticipantHandle.codec(), id);
        }
        buf.writeVarInt(inactiveBattles.size());
        for (final BattleParticipantHandle id : inactiveBattles) {
            buf.encode(NbtOps.INSTANCE, BattleParticipantHandle.codec(), id);
        }
        ServerPlayNetworking.send(entity, CHANNEL, buf);
    }

    private EntityBattlesUpdateSender() {
    }
}
