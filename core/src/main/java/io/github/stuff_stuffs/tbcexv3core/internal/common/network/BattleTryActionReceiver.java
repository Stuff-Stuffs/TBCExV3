package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class BattleTryActionReceiver {
    public static final Identifier IDENTIFIER = TBCExV3Core.createId("try_action");

    public static void init(final ServerPlayNetworkHandler handler) {
        ServerPlayNetworking.registerReceiver(handler, IDENTIFIER, BattleTryActionReceiver::receive);
    }

    private static void receive(final MinecraftServer server, final ServerPlayerEntity entity, final ServerPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        final BattleHandle handle = buf.decode(NbtOps.INSTANCE, BattleHandle.codec());
        final BattleAction action = buf.decode(NbtOps.INSTANCE, BattleAction.CODEC);
        server.execute(() -> {
            final ServerWorld world = server.getWorld(handle.getWorldKey());
            if (world != null) {
                final Battle battle = ((ServerBattleWorld) world).tryGetBattle(handle);
                if (battle != null) {
                    //TODO respond to client
                    battle.tryPushAction(action);
                }
            }
        });
    }

    private BattleTryActionReceiver() {
    }
}
