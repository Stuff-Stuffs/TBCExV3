package io.github.stuff_stuffs.tbcexv3core.internal.client.network;

import io.github.stuff_stuffs.tbcexv3core.api.battles.BattleHandle;
import io.github.stuff_stuffs.tbcexv3core.internal.common.TBCExPlayerEntity;
import io.github.stuff_stuffs.tbcexv3core.internal.common.network.PlayerCurrentBattleSender;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.jetbrains.annotations.Nullable;

public final class PlayerCurrentBattleReceiver {
    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(PlayerCurrentBattleSender.CHANNEL, PlayerCurrentBattleReceiver::receive);
    }

    private static void receive(final MinecraftClient client, final ClientPlayNetworkHandler handler, final PacketByteBuf buf, final PacketSender sender) {
        final boolean present = buf.readBoolean();
        @Nullable final BattleHandle handle;
        if (present) {
            handle = BattleHandle.of(RegistryKey.of(RegistryKeys.WORLD, buf.readIdentifier()), buf.readUuid());
        } else {
            handle = null;
        }
        client.execute(() -> ((TBCExPlayerEntity) client.player).tbcex$setCurrentBattle(handle));
    }

    private PlayerCurrentBattleReceiver() {
    }
}
