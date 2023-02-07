package io.github.stuff_stuffs.tbcexv3core.internal.common.network;

import net.minecraft.server.network.ServerPlayNetworkHandler;

public final class TBCExV3PlayNetworkingInit {
    public static void register(final ServerPlayNetworkHandler handler) {
        BattleUpdateRequestReceiver.init(handler);
        BattleTryActionReceiver.init(handler);
        EntityBattlesUpdateRequestReceiver.init(handler);
        PlayerCurrentBattleRequestReceiver.init(handler);
    }

    private TBCExV3PlayNetworkingInit() {
    }
}
