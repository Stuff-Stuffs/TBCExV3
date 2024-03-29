package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import io.github.stuff_stuffs.tbcexv3core.api.battles.Battle;
import io.github.stuff_stuffs.tbcexv3core.api.battles.ServerBattleWorld;
import io.github.stuff_stuffs.tbcexv3core.api.battles.action.BattleAction;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.BattleImpl;
import io.github.stuff_stuffs.tbcexv3core.impl.battle.environment.BattleEnvironmentImpl;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExV3Core;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BattleUpdateRequestReceiver {
    public static final Identifier CHANNEL = TBCExV3Core.createId("battle_update_request");

    public static void init(ServerPlayNetworkHandler handler) {
        ServerPlayNetworking.registerReceiver(handler, CHANNEL, BattleUpdateRequestReceiver::receive);
    }

    private static void receive(final MinecraftServer server, final ServerPlayerEntity entity, final ServerPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        final int count = buf.readVarInt();
        final List<BattleUpdateRequest> requests = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            requests.add(buf.decode(NbtOps.INSTANCE, BattleUpdateRequest.CODEC));
        }
        server.execute(() -> {
            final List<BattleUpdate> updates = new ArrayList<>(count);
            for (final BattleUpdateRequest request : requests) {
                final ServerWorld world = server.getWorld(request.handle().getWorldKey());
                if (world != null) {
                    final Battle battle = ((ServerBattleWorld) world).tryGetBattle(request.handle());
                    if (battle != null) {
                        final List<BattleAction> actions = new ArrayList<>();
                        final int size = battle.getActionCount();
                        if (request.lastKnownGoodState() + 1 >= 0) {
                            for (int i = request.lastKnownGoodState() + 1; i < size; i++) {
                                actions.add(battle.getAction(i));
                            }
                            final Optional<BattleUpdate.InitialData> opt;
                            if (request.lastKnownGoodState() == -1) {
                                opt = Optional.of(new BattleUpdate.InitialData(((BattleImpl) battle).environment(), battle.origin()));
                            } else {
                                opt = Optional.empty();
                            }
                            updates.add(new BattleUpdate(request.handle(), actions, request.lastKnownGoodState() + 1, opt));
                        }
                    }
                }
            }
            BattleUpdateSender.send(updates, sender, server.getRegistryManager().get(RegistryKeys.BIOME));
        });
    }

    private BattleUpdateRequestReceiver() {
    }
}
