package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class BattleUpdateRequestReceiver {
    public static final Identifier CHANNEL = TBCExV3Core.createId("battle_update_request");

    public static void init() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, BattleUpdateRequestReceiver::receive);
    }

    private static void receive(MinecraftServer server, ServerPlayerEntity entity, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        int count = buf.readVarInt();
        List<BattleUpdateRequest> requests = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            requests.add(buf.decode(BattleUpdateRequest.CODEC));
        }
        server.execute(() -> {
            List<BattleUpdate> updates = new ArrayList<>(count);
            for (BattleUpdateRequest request : requests) {
                final ServerWorld world = server.getWorld(request.handle().getWorldKey());
                if(world!=null) {
                    Battle battle = world.tryGetBattle(request.handle());
                    if (battle != null) {
                        List<BattleAction> actions = new ArrayList<>();
                        int size = battle.getActionCount();
                        if (request.lastKnownGoodState() + 1 >= 0) {
                            for (int i = request.lastKnownGoodState() + 1; i < size; i++) {
                                actions.add(battle.getAction(i));
                            }
                            updates.add(new BattleUpdate(request.handle(), actions, request.lastKnownGoodState() + 1));
                        }
                    }
                }
            }
            BattleUpdateSender.send(updates, sender);
        });
    }

    private BattleUpdateRequestReceiver() {
    }
}
